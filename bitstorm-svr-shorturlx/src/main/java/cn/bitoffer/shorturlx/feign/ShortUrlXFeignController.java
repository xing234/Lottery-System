package cn.bitoffer.shorturlx.feign;

import cn.bitoffer.api.feign.TestProviderClient;
import cn.bitoffer.shorturlx.model.UrlMap;
import cn.bitoffer.shorturlx.service.ShortUrlXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProviderFeignController implements TestProviderClient {

    @Autowired
    private ShortUrlXService shortUrlXService;
    // public String call(String name) {
    //
    //     UrlMap urlMap = new UrlMap(longUrl);
    //     urlMap.setExampleName("测试样例 AAA");
    //     urlMap.setStatus(1);
    //     shortUrlXService.save(urlMap);
    //     System.out.println(shortUrlXService.getExampleById(urlMap.getExampleId()));
    //     urlMap.setExampleName("测试样例 BBB");
    //     shortUrlXService.update(urlMap);
    //     System.out.println(shortUrlXService.getExampleById(urlMap.getExampleId()));
    //     shortUrlXService.deleteExampleById(urlMap.getExampleId());
    //     System.out.println(shortUrlXService.getExampleById(urlMap.getExampleId()));
    //
    //     //redis
    //     shortUrlXService.cacheExampleToRedis(urlMap);
    //     UrlMap urlMap1 = shortUrlXService.getExampleFromRedis(urlMap.getExampleId().toString());
    //     System.out.println(urlMap1);
    //
    //     return "服务提供者1：" + name;
    // }
}
