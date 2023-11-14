package cn.bitoffer.xtimer.service.trigger;

import cn.bitoffer.xtimer.common.conf.SchedulerAppConf;
import cn.bitoffer.xtimer.model.TaskModel;
import cn.bitoffer.xtimer.redis.ReentrantDistributeLock;
import cn.bitoffer.xtimer.service.executor.ExecutorWorker;
import cn.bitoffer.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class TriggerPoolTask {

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    ExecutorWorker executorWorker;

    @Async("triggerPool")
    public void runExecutor(TaskModel task) {
        log.info("start runExecutor");

        executorWorker.work(TimerUtils.UnionTimerIDUnix(task.getTimerId(),task.getRunTimer()));

        log.info("end executeAsync");
    }
}