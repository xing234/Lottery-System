package cn.bitoffer.shorturlx.service.impl;

import cn.bitoffer.shorturlx.mapper.ExampleMapper;
import cn.bitoffer.shorturlx.model.Example;
import cn.bitoffer.shorturlx.redis.RedisUtil;
import cn.bitoffer.shorturlx.service.ShortUrlXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl implements ShortUrlXService {

    @Autowired
    private ExampleMapper exampleMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void save(Example example) {
        exampleMapper.save(example);
    }

    @Override
    public void update(Example example) {
        exampleMapper.update(example);
    }

    @Override
    public Example getExampleById(Long id) {
        return exampleMapper.getExampleById(id);
    }

    @Override
    public void deleteExampleById(Long id) {

        exampleMapper.deleteById(id);
    }

    @Override
    public void cacheExampleToRedis(Example example) {
        redisUtil.set(example.getExampleId().toString(),example);
    }

    @Override
    public Example getExampleFromRedis(String exampleId) {
        Object obj = redisUtil.get(exampleId);
        Example example = null;
        if (obj != null){
            example = (Example) obj;
        }
        return example;
    }
}
