package cn.bitoffer.xtimer.feign;

import cn.bitoffer.api.feign.XTimerClient;
import cn.bitoffer.xtimer.model.Example;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class XTimerFeignController implements XTimerClient {

    @Autowired
    private ExampleService exampleService;
    public String call(String name) {

        Example example = new Example();
        example.setExampleName("测试样例 AAA");
        example.setStatus(1);
        exampleService.save(example);
        System.out.println(exampleService.getExampleById(example.getExampleId()));
        example.setExampleName("测试样例 BBB");
        exampleService.update(example);
        System.out.println(exampleService.getExampleById(example.getExampleId()));
        exampleService.deleteExampleById(example.getExampleId());
        System.out.println(exampleService.getExampleById(example.getExampleId()));

        //redis
        exampleService.cacheExampleToRedis(example);
        Example example1 = exampleService.getExampleFromRedis(example.getExampleId().toString());
        System.out.println(example1);

        return "服务提供者1：" + name;
    }
}
