package cn.bitoffer.xtimer.service.impl;

import cn.bitoffer.xtimer.common.ErrorCode;
import cn.bitoffer.xtimer.common.conf.MigratorAppConf;
import cn.bitoffer.xtimer.common.conf.SchedulerAppConf;
import cn.bitoffer.xtimer.enums.TaskStatus;
import cn.bitoffer.xtimer.enums.TimerStatus;
import cn.bitoffer.xtimer.exception.BusinessException;
import cn.bitoffer.xtimer.mapper.TaskMapper;
import cn.bitoffer.xtimer.mapper.TimerMapper;
import cn.bitoffer.xtimer.model.TaskModel;
import cn.bitoffer.xtimer.model.TimerModel;
import cn.bitoffer.xtimer.redis.ReentrantDistributeLock;
import cn.bitoffer.xtimer.redis.TaskCache;
import cn.bitoffer.xtimer.service.XTimerService;
import cn.bitoffer.xtimer.utils.TimerUtils;
import cn.bitoffer.xtimer.vo.TimerVO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class XTimerServiceImpl implements XTimerService {

    @Autowired
    private TimerMapper timerMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    private MigratorAppConf migratorAppConf;

    @Autowired
    private TaskCache taskCache;

    private static final int  defaultGapSeconds= 3;

    @Override
    public Long CreateTimer(TimerVO timerVO) {
        String lockToken = TimerUtils.GetTokenStr();
        // 只加锁不解锁，只有超时解锁；超时时间控制频率；
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetCreateLockKey(timerVO.getApp()),
                lockToken,
                defaultGapSeconds);
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建/删除操作过于频繁，请稍后再试！");
        }

        boolean isValidCron = CronExpression.isValidExpression(timerVO.getCron());
        if(!isValidCron){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"invalid cron");
        }

        TimerModel timerModel = TimerVO.voToObj(timerVO);
        if (timerModel == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        timerMapper.save(timerModel);
        return timerModel.getTimerId();
    }

    @Override
    public void DeleteTimer(String app, long id) {
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetCreateLockKey(app),
                lockToken,
                defaultGapSeconds);
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建/删除操作过于频繁，请稍后再试！");
        }

        timerMapper.deleteById(id);
    }

    @Override
    public void Update(TimerVO timerVO) {
        TimerModel timerModel = TimerVO.voToObj(timerVO);
        if (timerModel == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        timerMapper.update(timerModel);
    }

    @Override
    public TimerVO GetTimer(String app, long id) {
        TimerModel timerModel  = timerMapper.getTimerById(id);
        TimerVO timerVO = TimerVO.objToVo(timerModel);
        return timerVO;
    }

    @Override
    public void EnableTimer(String app, long id) {
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetEnableLockKey(app),
                lockToken,
                defaultGapSeconds);
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"激活/去激活操作过于频繁，请稍后再试！");
        }

        // 激活逻辑
        doEnableTimer(id);
    }

    @Transactional
    public void doEnableTimer(long id){
        // 1. 数据库获取Timer
        TimerModel timerModel = timerMapper.getTimerById(id);
        // 2. 校验状态
        if(timerModel.getStatus() != TimerStatus.Unable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Unable状态，激活失败，id:"+id);
        }

        // 3.取得批量的执行时机
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(timerModel.getCron());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败："+timerModel.getCron());
        }
        Date now = new Date();
        Date end = TimerUtils.GetForwardTwoMigrateStepEnd(now,migratorAppConf.getMigrateStepMinutes());

        List<Long> executeTimes = TimerUtils.GetCronNextsBetween(cronExpression,now,end);
        if (CollectionUtils.isEmpty(executeTimes) ){
            log.warn("获取执行时机 executeTimes 为空");
            return;
        }
        // 执行时机加入数据库
        List<TaskModel> taskList = batchTasksFromTimer(timerModel,executeTimes);
        // 基于 timer_id + run_timer 唯一键，保证任务不被重复插入
        taskMapper.batchSave(taskList);

        // 执行时机加入 redis ZSet
        boolean cacheRes = taskCache.cacheSaveTasks(taskList);
        if(!cacheRes){
            log.error("Zset存储taskList失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ZSet存储taskList失败，timerId:"+timerModel.getTimerId());
        }

        // 修改 timer 状态为激活态
        timerModel.setStatus(TimerStatus.Enable.getStatus());
        timerMapper.update(timerModel);
    }

    private List<TaskModel> batchTasksFromTimer(TimerModel timerModel, List<Long> executeTimes){
        if(timerModel == null || CollectionUtils.isEmpty(executeTimes)){
            return null;
        }

        List<TaskModel> taskList = new ArrayList<>();
        for (Long runTimer:executeTimes) {
            TaskModel task = new TaskModel();
            task.setApp(timerModel.getApp());
            task.setTimerId(timerModel.getTimerId());
            task.setRunTimer(runTimer);
            task.setStatus(TaskStatus.NotRun.getStatus());
            taskList.add(task);
        }
        return taskList;
    }


    @Override
    public void UnEnableTimer(String app, long id) {
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetEnableLockKey(app),
                lockToken,
                defaultGapSeconds);
        if(!ok){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"激活/去激活操作过于频繁，请稍后再试！");
        }

        // 去激活逻辑
        doUnEnableTimer(id);
    }

    @Transactional
    public void doUnEnableTimer(Long id){
        // 1. 数据库获取Timer
        TimerModel timerModel = timerMapper.getTimerById(id);
        // 2. 校验状态
        if(timerModel.getStatus() != TimerStatus.Unable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Unable状态，去激活失败，id:"+id);
        }
        timerModel.setStatus(TimerStatus.Unable.getStatus());
        timerMapper.update(timerModel);
    }


    @Override
    public List<TimerVO> GetAppTimers(String app) {
        return null;
    }
}
