package cn.bitoffer.seckill.service.impl;

import cn.bitoffer.seckill.mapper.*;
import cn.bitoffer.seckill.model.*;
import cn.bitoffer.seckill.redis.RedisUtil;
import cn.bitoffer.seckill.service.SecKillService;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private ExampleMapper exampleMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SecKillRecordMapper recordMapper;

    @Autowired
    private SecKillStockMapper stockMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisUtil redisUtil;

    private final KafkaTemplate<String, Object> kafkaTemplate;



    @Override
    public Goods getGoodsByNum(String traceID, Long userID, String goodsNum) {
        return goodsMapper.getGoodsByNum(goodsNum);
    }

    @Override
    public ArrayList<Goods> getGoodsList(String traceID, Long userID, Integer offset, Integer limit) {
        return goodsMapper.getGoodsList(offset, limit);
    }

    @Override
    @Transactional
    public String secKillV1(String traceID, Long userID, String goodsNum, Integer num) {
        Goods goods = goodsMapper.getGoodsByNum(goodsNum);
        return secKillInStore(traceID, userID, goods, num);
    }

    @Override
    @Transactional
    public String secKillV2(String traceID, Long userID, String goodsNum, Integer num) {
        Goods goods = goodsMapper.getGoodsByNum(goodsNum);
        String secNum = UUID.randomUUID().toString();
        try {
            preDescStock(traceID, userID, goods, num, secNum, "");
        } catch(Exception e) {
            System.out.println("preDescStock err "+e.getMessage());
            return "";
        }
        return secKillInStore(traceID, userID, goods, num);
    }

    @Override
    public String secKillV3(String traceID, Long userID, String goodsNum, Integer num) {
        Goods goods = goodsMapper.getGoodsByNum(goodsNum);
        String secNum = UUID.randomUUID().toString();
        try {
            preDescStock(traceID, userID, goods, num, secNum, "");
        } catch(Exception e) {
            System.out.println("preDescStock err "+e.getMessage());
            return "";
        }
        kafkaTemplate.send("user", user).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.info("sendMessage onFailure:{}", throwable.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                log.info("sendMessage onSuccess:{},{},{}", result.getRecordMetadata().topic(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });


        return secKillInStore(traceID, userID, goods, num);
    }

    public void preDescStock(String traceID, Long userID, Goods goods, Integer num, String secNum, String secRecord) throws Exception {
        Date date = new Date();
        PreSecKillRecord psRecord = new PreSecKillRecord();
        psRecord.setGoodsID(goods.getID());
        psRecord.setPrice(goods.getPrice());
        psRecord.setSecNum(secNum);
        psRecord.setPrice(goods.getPrice());
        psRecord.setStatus(SKStatusEnum.SK_STATUS_BEFORE_ORDER.getValue());
        psRecord.setCreateTime(date);
        psRecord.setModifyTime(date);
        Gson gson = new Gson();
        String recordStr = gson.toJson(psRecord);
        List<Object> result = redisUtil.executeLua(secKillLua, Arrays.asList(""+userID, ""+goods.getID(), ""+num, secNum, recordStr));
        Long y = Long.valueOf(String.valueOf(result.get(0))).longValue();
        Integer x = y.intValue();
        System.out.println("x is : " + x);
        switch(x)
        {
            case -1:
                throw new Exception("already in sec kill");
            case -2:
                throw new Exception("user out of limit on this goods");
            case -3:
                throw new Exception("stock not enough");
            case -4:
                throw new Exception("killed out");
            default:
                break;
        }
    }
    @Transactional
    public String secKillInStore(String traceID, Long userID, Goods goods, Integer num) {
        try {
            String secNum = UUID.randomUUID().toString();
            String orderNum = UUID.randomUUID().toString();
            stockMapper.descStock(goods.getID(), num);
            Order order = new Order();
            order.setBuyer(userID);
            order.setSeller(goods.getSeller());
            order.setGoodsID(goods.getID());
            order.setGoodsNum(goods.getGoodsNum());
            order.setPrice(goods.getPrice());
            order.setOrderNum(orderNum);
            order.setStatus(SKStatusEnum.SK_STATUS_BEFORE_PAY.getValue());
            orderMapper.save(order);
            SecKillRecord record = new SecKillRecord();
            record.setUserID(userID);
            record.setGoodsID(goods.getID());
            record.setPrice(goods.getPrice());
            record.setOrderNum(order.getOrderNum());
            record.setSecNum(secNum);
            record.setStatus(SKStatusEnum.SK_STATUS_BEFORE_PAY.getValue());
            recordMapper.save(record);
            return orderNum;
        } catch (Exception e) {
            String message = e.getMessage();
            System.out.println(message);
            if (message.contains("PRIMARY")) {
                message = message + "主键重复";
            }
            throw new RuntimeException(message);
        }
    }

    public static class SecKillMsg{
        public String traceID;
        public Goods goods;
        public String secNum;
        public Long userID;
        public Integer num;
    }

    private String secKillLua = "-- key1：用户id，key2：商品id key3：抢购多少个 key4：秒杀单号, keys5:秒杀记录\n" +
            "-- keyLimit是 SK:Limit:goodsID\n" +
            "local keyLimit = \"SK:Limit\" .. KEYS[2]\n" +
            "-- keyUserGoodsSecNum 是 SK:UserGoodsSecNum:goodsID:userID\n" +
            "local keyUserGoodsSecNum = \"SK:UserGoodsSecNum:\" .. KEYS[1] .. \":\" .. KEYS[2]\n" +
            "-- keyUserSecKilledNum 是SK:UserSecKilledNum:userID:goodsID\n" +
            "local keyUserSecKilledNum = \"SK:UserSecKilledNum:\" .. KEYS[1] .. \":\" .. KEYS[2]\n" +
            "\n" +
            "--1.判断这个用户是不是已经在秒杀中，是的话返回secNum\n" +
            "local alreadySecNum = redis.call('get', keyUserGoodsSecNum)\n" +
            "\n" +
            "local retAry = {0, \"\"}\n" +
            "if alreadySecNum and string.len(alreadySecNum) ~= 0 then\n" +
            "   retAry[1] = -1\n" +
            "   retAry[2] = alreadySecNum\n" +
            "   return retAry\n" +
            "end\n" +
            "\n" +
            "--2.判断这个用户是不是已经超过限额了\n" +
            "local limit = redis.call('get', keyLimit)\n" +
            "local userSecKilledNum  = redis.call('get', keyUserSecKilledNum)\n" +
            "if limit and userSecKilledNum and tonumber(userSecKilledNum) + tonumber(num) > tonumber(limit) then \n" +
            "   retAry[1] = -2\n" +
            "   return retAry\n" +
            "end\n" +
            "\n" +
            "--3.判断查询活动库存\n" +
            "local stockKey = \"SK:Stock:\" .. KEYS[2]\n" +
            "local stock = redis.call('get', stockKey)\n" +
            "if not stock or tonumber(stock) < tonumber(KEYS[3]) then\n" +
            "   retAry[1] = -3\n" +
            "   return retAry\n" +
            "end\n" +
            "\n" +
            "-- 4.活动库存充足，进行扣减操作\n" +
            "redis.call('decrby',stockKey, KEYS[3])\n" +
            "redis.call('incrby', keyUserSecKilledNum, KEYS[3])\n" +
            "redis.call('set', keyUserGoodsSecNum, KEYS[4]) \n" +
            "redis.call('set', KEYS[4], KEYS[5]) \n" +
            "return retAry";


}
