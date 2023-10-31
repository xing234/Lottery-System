package cn.bitoffer.xtimer.common.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SchedulerAppConf {

    @Value("${scheduler.bucketsNum}")
    private int bucketsNum;
    @Value("${scheduler.tryLockSeconds}")
    private int tryLockSeconds;
    @Value("${scheduler.tryLockGapMilliSeconds}")
    private int tryLockGapMilliSeconds;
    @Value("${scheduler.successExpireSeconds}")
    private int successExpireSeconds;

    public int getBucketsNum() {
        return bucketsNum;
    }

    public void setBucketsNum(int bucketsNum) {
        this.bucketsNum = bucketsNum;
    }

    public int getTryLockSeconds() {
        return tryLockSeconds;
    }

    public void setTryLockSeconds(int tryLockSeconds) {
        this.tryLockSeconds = tryLockSeconds;
    }

    public int getTryLockGapMilliSeconds() {
        return tryLockGapMilliSeconds;
    }

    public void setTryLockGapMilliSeconds(int tryLockGapMilliSeconds) {
        this.tryLockGapMilliSeconds = tryLockGapMilliSeconds;
    }

    public int getSuccessExpireSeconds() {
        return successExpireSeconds;
    }

    public void setSuccessExpireSeconds(int successExpireSeconds) {
        this.successExpireSeconds = successExpireSeconds;
    }
}