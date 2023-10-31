package cn.bitoffer.xtimer.service.scheduler;

import org.springframework.scheduling.annotation.Scheduled;

public class SchedulerWorker {
    @Scheduled(cron="0/1 * * * * ?")//每秒钟执行一次，以空格分隔
    public void cron(){
        System.out.println("spring task 这是定时任务，时间是：");
    }
}
