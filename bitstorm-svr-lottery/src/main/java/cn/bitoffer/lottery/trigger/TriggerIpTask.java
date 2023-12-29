package cn.bitoffer.lottery.trigger;

import cn.bitoffer.lottery.constant.Constants;
import cn.bitoffer.lottery.redis.RedisUtil;
import cn.bitoffer.xtimer.service.trigger.TriggerTimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class TriggerIpTask extends TimerTask {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(){
        for(int i=0;i< Constants.ipFrameSize;i++){
            String key = String.format(Constants.ipLotteryDayNumPrefix+"%d", i);
            //  清理ip抽奖次数
            redisUtil.del(key);
        }
    }
}
