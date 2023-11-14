package cn.bitoffer.xtimer.service.scheduler;

import cn.bitoffer.xtimer.common.ErrorCode;
import cn.bitoffer.xtimer.common.conf.SchedulerAppConf;
import cn.bitoffer.xtimer.exception.BusinessException;
import cn.bitoffer.xtimer.redis.ReentrantDistributeLock;
import cn.bitoffer.xtimer.service.trigger.TriggerWorker;
import cn.bitoffer.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Component
public class SchedulerTask {

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    @Autowired
    TriggerWorker triggerWorker;

    @Async("schedulerPool")
    public void asyncHandleSlice(Date date,int bucketId) {
        log.info("start executeAsync");

        String lockToken = TimerUtils.GetTokenStr();
        // 只加锁不解锁，只有超时解锁；超时时间控制频率；
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetTimeBucketLockKey(date,bucketId),
                lockToken,
                schedulerAppConf.getTryLockSeconds());
        if(!ok){
            log.error("asyncHandleSlice 获取分布式锁失败");
            return;
        }

        log.info("get scheduler lock success, key: %s", TimerUtils.GetTimeBucketLockKey(date, bucketId));

        // 调用trigger进行处理
        triggerWorker.work(TimerUtils.GetSliceMsgKey(date,bucketId));


        // todo 成功之后更新分布式锁的过期时间 ack


        log.info("end executeAsync");
    }
}
