package cn.bitoffer.xtimer.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class SchedulerTask {
    @Async("asyncServiceExecutor")
    public void asyncHandleSlice(int bucketId) {
        log.info("start executeAsync");

        System.out.println("异步线程要做的事情"+":bucketId"+bucketId);

        log.info("end executeAsync");
    }
}
