package cn.bitoffer.xtimer.service.scheduler;

import cn.bitoffer.xtimer.common.conf.SchedulerAppConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class SchedulerWorker {

    @Autowired
    SchedulerTask schedulerTask;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    @Scheduled(fixedRate = 1000)
    public void scheduledTask() {
        System.out.println("任务执行时间：" + LocalDateTime.now());
        handleSlices();
    }
    
    private void handleSlices(){
        for (int i = 0; i < 5; i++) {
            handleSlice(i);
        }
    }

    private void handleSlice(int bucketId){
        Date now = new Date();
        schedulerTask.asyncHandleSlice(bucketId);

    }

    private int getValidBucket(){
        return schedulerAppConf.getBucketsNum();
    }


}
