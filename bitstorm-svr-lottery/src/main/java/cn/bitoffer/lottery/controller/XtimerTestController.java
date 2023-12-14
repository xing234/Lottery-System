package cn.bitoffer.lottery.controller;

import cn.bitoffer.api.dto.xtimer.NotifyHTTPParam;
import cn.bitoffer.api.dto.xtimer.TimerDTO;
import cn.bitoffer.api.feign.XTimerClient;
import cn.bitoffer.lottery.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/xtimer")
@Slf4j
public class XtimerTestController {

    @Resource
    private XTimerClient xTimerClient;

    @GetMapping("/createTimer")
    public ResponseEntity<String> createTimer() {
        TimerDTO timerDTO = new TimerDTO();
        timerDTO.setApp("testConsumer");
        timerDTO.setName("测试Xtimer001");
        timerDTO.setCron("0 * * ? * *"); //每分钟执行一次

        NotifyHTTPParam notifyHTTPParam = new NotifyHTTPParam();
        notifyHTTPParam.setUrl("http://127.0.0.1:8093/xtimer/callback");
        notifyHTTPParam.setMethod("POST");
        notifyHTTPParam.setBody("时间到了，回执此信息");
        timerDTO.setNotifyHTTPParam(notifyHTTPParam);

        Long timerId = xTimerClient.createTimer(timerDTO);
        log.info("TimerId:" + timerId);
        return ResponseEntity.ok(
                "timerId:"+timerId
        );
    }

    @GetMapping("/enableTimer")
    public ResponseEntity<String> enableTimer() {
        TimerDTO timerDTO = new TimerDTO();
        timerDTO.setTimerId(1L);
        timerDTO.setApp("testConsumer");

        xTimerClient.enableTimer("testConsumer",1L);
        log.info("enableTimer success");
        return ResponseEntity.ok(
                "enable success:"
        );
    }

    @PostMapping(value = "/callback")
    public void createTask(@RequestBody String callBack){
        System.out.println("收到Callback:"+callBack);
    }
}
