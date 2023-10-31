package cn.bitoffer.testconsumer.controller;

import cn.bitoffer.api.feign.TestProviderClient;
import cn.bitoffer.testconsumer.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/example")
@Slf4j
public class ConsumerController {

    @Resource
    private TestProviderClient providerClient;

    @GetMapping("/consumer")
    public ResponseEntity<String> consumer() {
        String resultStr = providerClient.call("hello");
        System.out.println("AA:" + resultStr);
        log.info("BB:" + resultStr);
        return ResponseEntity.ok(
                "result:"+resultStr
        );
    }
}
