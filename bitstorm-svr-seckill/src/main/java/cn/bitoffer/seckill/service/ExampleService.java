package cn.bitoffer.seckill.service;

import cn.bitoffer.seckill.model.Example;
import cn.bitoffer.seckill.model.Goods;

public interface ExampleService {

    Goods getGoodsByNum(String goodsNum);

    void save(Example example);

    void update(Example example);

    Example getExampleById(Long id);

    void deleteExampleById(Long id);

    void cacheExampleToRedis(Example example);

    Example getExampleFromRedis(String exampleId);

}
