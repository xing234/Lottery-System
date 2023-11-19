package cn.bitoffer.testprovider.feign;

import cn.bitoffer.api.feign.TestProviderClient;
import cn.bitoffer.testprovider.model.Example;
import cn.bitoffer.testprovider.service.ExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProviderFeignController implements TestProviderClient {

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

        //redis lua


        System.out.println(example1);

        return "服务提供者1：" + name;
    }
}
