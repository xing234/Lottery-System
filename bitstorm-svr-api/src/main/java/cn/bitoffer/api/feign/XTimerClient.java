package cn.bitoffer.api.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("bitstorm-svr-xtimer")
public interface XTimerClient {

    /**
     * 测试
     *
     */
    @GetMapping(value = "/call")
    public String call(@RequestParam("name") String name);

}
