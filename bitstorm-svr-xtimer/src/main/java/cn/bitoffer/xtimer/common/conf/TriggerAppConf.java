package cn.bitoffer.xtimer.common.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TriggerAppConf {
    @Value("${trigger.zrangeGapSeconds}")
    private int zrangeGapSeconds;
    @Value("${trigger.workersNum}")
    private int workersNum;
}