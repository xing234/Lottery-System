package cn.bitoffer.testprovider.service;

import cn.bitoffer.testprovider.model.Example;

public interface ExampleService {

    void save(Example example);

    void update(Example example);

    Example getExampleById(Long id);

    void deleteExampleById(Long id);

    void cacheExampleToRedis(Example example);

    Example getExampleFromRedis(String exampleId);

}
