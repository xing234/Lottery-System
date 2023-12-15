package cn.bitoffer.testconsumer.controller;

import cn.bitoffer.api.feign.TestProviderClient;
import cn.bitoffer.common.model.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/demo")
@Slf4j
public class ConsumerController {

    @Resource
    private TestProviderClient providerClient;

    @GetMapping("/example")
    public ResponseEntity<String> consumer() {
        String resultStr = providerClient.call("hello");
        log.info("Result:" + resultStr);
        return ResponseEntity.ok(
                "result:"+resultStr
        );
    }
}
