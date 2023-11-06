package cn.bitoffer.api.feign;


import cn.bitoffer.api.dto.xtimer.TimerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("bitstorm-svr-xtimer")
public interface XTimerClient {

    /**
     * 测试
     *
     */
    @GetMapping(value = "/call")
    public String call(@RequestParam("name") String name);

    @PostMapping(value = "/createTimer")
    public Long createTimer(@RequestBody TimerDTO timerDTO);

    @PostMapping(value = "/enableTimer")
    public void enableTimer(@RequestBody TimerDTO timerDTO);

}
