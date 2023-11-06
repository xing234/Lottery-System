package cn.bitoffer.xtimer.redis;

import cn.bitoffer.xtimer.common.ErrorCode;
import cn.bitoffer.xtimer.common.conf.SchedulerAppConf;
import cn.bitoffer.xtimer.exception.BusinessException;
import cn.bitoffer.xtimer.model.TaskModel;
import cn.bitoffer.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class TaskCache {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    public String GetTableName(TaskModel taskModel){
        int maxBucket = schedulerAppConf.getBucketsNum();

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(new Date(taskModel.getRunTimer()));
        long index = taskModel.getTimerId()%maxBucket;
        return sb.append(timeStr).append("_").append(index).toString();
    }

    public boolean cacheSaveTasks(List<TaskModel> taskList){
        try {
            redisTemplate.setEnableTransactionSupport(true);
            redisTemplate.multi();
            for (TaskModel task : taskList) {
                long unix = task.getRunTimer();
                String tableName = GetTableName(task);
                redisTemplate.opsForZSet().add(
                        tableName,
                        TimerUtils.UnionTimerIDUnix(task.getTimerId(), unix),
                        unix);
            }
            redisTemplate.exec();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<TaskModel> getTasksFromCache(String key,long start, long end){
        List<TaskModel> tasks = new ArrayList<>();

        Set<Object> timerIDUnixs = redisTemplate.opsForZSet().rangeByScore(key,start,end-1);
        if(CollectionUtils.isEmpty(timerIDUnixs)){
            return tasks;
        }

        for (Object timerIDUnixObj:timerIDUnixs) {
            TaskModel task = new TaskModel();
            String timerIDUnix = (String) timerIDUnixObj;
            List<Long> longSet = TimerUtils.SplitTimerIDUnix(timerIDUnix);
            if(longSet.size() != 2){
                log.error("splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnix);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"splitTimerIDUnix 错误, timerIDUnix:"+timerIDUnix);
            }
            task.setTimerId(longSet.get(0));
            task.setTimerId(longSet.get(1));
            tasks.add(task);
        }

        return tasks;
    }

}
