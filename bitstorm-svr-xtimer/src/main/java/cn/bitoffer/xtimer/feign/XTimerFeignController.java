package cn.bitoffer.xtimer.feign;

import cn.bitoffer.api.feign.XTimerClient;
import cn.bitoffer.xtimer.model.Example;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class XTimerFeignController implements XTimerClient {

    public String call(String name) {

        Example example = new Example();
        example.setExampleName("测试样例 AAA");
        example.setStatus(1);

        return "服务提供者1：" + name;
    }
}
