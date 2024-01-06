package cn.bitoffer.lottery.scheduler;

import cn.bitoffer.lottery.cache.CacheMgr;
import cn.bitoffer.lottery.constant.Constants;
import cn.bitoffer.lottery.mapper.PrizeMapper;
import cn.bitoffer.lottery.model.Prize;
import cn.bitoffer.lottery.service.impl.LotteryServiceImpl2;
import cn.bitoffer.lottery.service.impl.LotteryServiceImpl3;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ShedulerPrizePoolTask {
    @Autowired
    private CacheMgr cacheMgr;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private LotteryServiceImpl3 lotteryServiceImpl3;

    public int incrPrizePool(Long prizeId,int num) {
        String key = Constants.prizePoolCacheKey;
        int ret = cacheMgr.addPrizeInPrizePool(key,prizeId.toString(),num);
        if(ret < num){
            int left = num - ret;
            ret = cacheMgr.addPrizeInPrizePool(key,prizeId.toString(),num);
        }
        return ret;
    }

//    @Scheduled(cron = "0 * * * * *")
//    @PostConstruct
    public void fillPrizePool() throws ParseException {
        log.info("Resetting prize pool!!!!!");
        int totalNum = 0;
        ArrayList<Prize> prizeList = prizeMapper.getAll();
        if (prizeList == null || prizeList.isEmpty()){
            return;
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Prize prize : prizeList){
            // sys_status = 1 表示有效
            if(prize.getSysStatus() != 1){
                continue;
            }
            if(prize.getPrizeNum()<=0 || prize.getLeftNum()<=0){
                continue;
            }
            if(prize.getBeginTime().after(now) || prize.getEndTime().before(now)){
                continue;
            }
            // 发奖计划数据不正确
            if(prize.getPrizePlan().length()<=7){
                continue;
            }
            List<TimePrizeInfo> prizePlanList = JSONArray.parseArray(prize.getPrizePlan(),TimePrizeInfo.class);
            int index = 0,prizeNum = 0;
            for(int i=0;i<prizePlanList.size();i++) {
                log.debug("fillPrizePool|prize_id={}\n, prizePlanInfo={}", prize.getId(), prizePlanList.get(i));
                TimePrizeInfo prizePlanInfo = prizePlanList.get(i);
                Date t = null;
                try {
                    t = sdf.parse(prizePlanInfo.getTime()); //Mon Jan 14 00:00:00 CST 2013
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(t.after(now)){
                    break;
                }
                // 该类奖品中，之前没有发放的奖品数量都要放入奖品池
                prizeNum += prizePlanInfo.getNum();
                index = i + 1;
            }
            if(prizeNum>0){
                incrPrizePool(prize.getId(),prizeNum);
                totalNum += prizeNum;
            }
            // 更新发奖计划
            if(index > 0){
                if(index<prizePlanList.size()){
                    prizePlanList = prizePlanList.subList(index,prizePlanList.size());
                }else {
                    prizePlanList = new ArrayList<>();
                }
                String prizePlanListStr = JSONArray.toJSONString(prizePlanList);
                lotteryServiceImpl3.updatePrizePlanWithCache(prize.getId(),prizePlanListStr);
            }
            if(totalNum > 0) {
                // 将更新后的数据加载到缓存中，因为上面updatePrizePlanWithCache会把缓存中的数据清空
                lotteryServiceImpl3.getAllUsefulPrizesWithCache(now);
            }
        }
        log.debug("fillPrizePool with num: {}", totalNum);
    }
}
