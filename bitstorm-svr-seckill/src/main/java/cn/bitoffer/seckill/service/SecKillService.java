package cn.bitoffer.seckill.service;

import cn.bitoffer.seckill.model.Goods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface SecKillService {

    Goods getGoodsByNum(String traceID, Long userID, String goodsNum);

    ArrayList<Goods> getGoodsList(String traceID, Long userID, Integer offset, Integer limit);

    String secKillV1(String traceID, Long userID, String goodsNum, Integer num);

    String secKillV2(String traceID, Long userID, String goodsNum, Integer num);

    String secKillV3(String traceID, Long userID, String goodsNum, Integer num);
}
