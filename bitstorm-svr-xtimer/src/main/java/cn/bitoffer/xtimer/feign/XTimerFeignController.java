package cn.bitoffer.xtimer.feign;

import cn.bitoffer.api.dto.xtimer.TimerDTO;
import cn.bitoffer.api.feign.XTimerClient;
import cn.bitoffer.xtimer.model.Example;
import cn.bitoffer.xtimer.service.XTimerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class XTimerFeignController implements XTimerClient {

    @Autowired
    XTimerService xTimerService;

    public String call(String name) {

        Example example = new Example();
        example.setExampleName("测试样例 AAA");
        example.setStatus(1);

        return "服务提供者1：" + name;
    }

    @Override
    public Long createTimer(TimerDTO timerDTO) {
        return xTimerService.CreateTimer(timerDTO);
    }

    @Override
    public void enableTimer(TimerDTO timerDTO) {
        xTimerService.EnableTimer(timerDTO.getApp(), timerDTO.getTimerId());
    }
}
