package cn.bitoffer.seckill.feign;

import cn.bitoffer.api.feign.TestProviderClient;
import cn.bitoffer.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SecKillFeignController implements TestProviderClient {

    @Autowired
    private SecKillService secKillService;
    public String call(String name) {
        return "";
    }
}
