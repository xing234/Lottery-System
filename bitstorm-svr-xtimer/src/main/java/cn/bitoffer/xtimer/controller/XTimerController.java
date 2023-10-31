package cn.bitoffer.xtimer.controller;

import cn.bitoffer.api.feign.TestProviderClient;
import cn.bitoffer.xtimer.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/example")
@Slf4j
public class XTimerController {

    @GetMapping("/consumer")
    public ResponseEntity<String> consumer() {
        return null;
    }
}
