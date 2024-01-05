package cn.bitoffer.lottery.service.impl;

import cn.bitoffer.lottery.common.ErrorCode;
import cn.bitoffer.lottery.constant.Constants;
import cn.bitoffer.lottery.model.CheckResult;
import cn.bitoffer.lottery.model.LotteryPrizeInfo;
import cn.bitoffer.lottery.model.LotteryResult;
import cn.bitoffer.lottery.model.LotteryUserInfo;
import cn.bitoffer.lottery.service.LotteryService;
import cn.bitoffer.lottery.utils.UtilTools;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@Slf4j
public class LotteryServiceImpl3 extends LotteryServiceImpl2 implements LotteryService {
    public LotteryResult lottery(Long userID, String userName, String ip) throws ParseException {
        LotteryResult lotteryResult = new LotteryResult();
        String lockKey = String.format(Constants.lotteryLockKeyPrefix+"%d", userID);
        // 在spring boot 2.0.6版本中整合的redisson,key和锁不能一样
        // redis setnx 操作,此处的lockKey在后面追加1是为了避免redisson锁时报错, 需要和待锁住的数据的key信息不同
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        /*
         ******** 抽奖逻辑*******
         */
        CheckResult checkResult = new CheckResult();
        // 1. 验证用户今日抽奖次数
        if (!checkUserDayLotteryTimesWithCache(userID)) {
            log.info("lotteryV3|CheckUserDayLotteryTimes failed，user_id：{}", userID);
            lotteryResult.setErrcode(ErrorCode.ERR_USER_LIMIT_INVALID);
            return lotteryResult;
        }
        // 2. 验证当天IP参与的抽奖次数
        if (!checkIpLimit(ip)) {
            log.info("lotteryV3|checkIpLimit failed，ip：{}", ip);
            lotteryResult.setErrcode(ErrorCode.ERR_IP_LIMIT_INVALID);
            return lotteryResult;
        }

        // 3. 验证IP是否在ip黑名单
        CheckResult ipCheckResult = checkBlackIpWithCache(ip);
        checkResult.setBlackIp(ipCheckResult.getBlackIp());
        if (!ipCheckResult.isOk()) {
            log.info("lotteryV3|checkBlackIp failed，ip：{}", ip);
            lotteryResult.setErrcode(ErrorCode.ERR_BLACK_IP);
            return lotteryResult;
        }

        // 4. 验证用户是否在用户黑名单
        CheckResult userCheckResult = checkBlackUserWithCache(userID);
        checkResult.setBlackUser(userCheckResult.getBlackUser());
        if (!userCheckResult.isOk()) {
            log.info("lotteryV3|checkBlackUser failed，user_id：{}", userID);
            lotteryResult.setErrcode(ErrorCode.ERR_BLACK_USER);
            return lotteryResult;
        }
        checkResult.setOk(true);

        // 5. 奖品匹配
        int prizeCode = UtilTools.getRandom(Constants.prizeCodeMax);
        LotteryPrizeInfo prize = getPrizeWithCache(prizeCode);
        if (prize == null)  {
            log.info("lotteryV3|getPrize null");
            lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
            return lotteryResult;
        }
        if (prize.getPrizeNum() <=0) {
            log.info("lotteryV3|prize_num invalid,prize_id: {}", prize.getId());
            lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
            return lotteryResult;
        }

        // 6.从奖品池发奖
        int num = getPrizeWithPool(prize.getId());
        // 奖品池奖品不够，不能发奖
        if (num <= 0) {
            log.info("lotteryV3|prize pool not enough,prize_id: {}", prize.getId());
            lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
            return lotteryResult;
        }
        if (!giveOutPrizeWithPool(prize.getId())) {
            log.info("lotteryV3|prize left not enough,prize_id: {}", prize.getId());
            lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
            return lotteryResult;
        }

        // 7. 发放优惠券
        if (prize.getPrizeType() == Constants.prizeTypeCouponDiff) {
            String code = prizeCodeDiff(prize.getId());
            if (code.isEmpty()) {
                log.info("lotteryV1|coupon code is empty: prize_id: {}", prize.getId());
                lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
                return lotteryResult;
            }
            // 填充优惠券编码
            prize.setCouponCode(code);
        }
        lotteryResult.setLotteryPrize(prize);
        // 8.记录中奖记录
        logLotteryResult(prize,userID,ip,userName,prizeCode);
        // 9. 大奖黑名单处理
        if (prize.getPrizeType() == Constants.prizeTypeEntityLarge) {
            LotteryUserInfo lotteryUserInfo = new LotteryUserInfo();
            lotteryUserInfo.setUserId(userID);
            lotteryUserInfo.setUserName(userName);
            lotteryUserInfo.setIp(ip);
            prizeLargeBlackLimit(checkResult.getBlackUser(),checkResult.getBlackIp(),lotteryUserInfo);
        }
        lock.unlock();
        return lotteryResult;
    }

    public int getPrizeWithPool(Long prizeId) {
        return cacheMgr.getPrizeNumByPool(prizeId);
    }

    public boolean giveOutPrizeWithPool(Long prizeId) {
        int cnt = cacheMgr.decrPrizeLeftNumByPool(prizeId);
        if (cnt < 0) {
            log.info("giveOutPrizeWithPool|prize_pool not enough.prize_id: {}",prizeId);
            return false;
        }
        return giveOutPrize(prizeId);
    }

    public void updatePrizePlanWithCache(Long prizeId,String prizePlan){
        // 清空缓存
        cacheMgr.updatePrizeByCache(prizeId);
        // 将db里对应奖品的prize_plan修改为空
        prizeMapper.updatePrizePlan(prizeId,prizePlan);
    }

    public void updatePrizePlanAndTimeWithCache(Long prizeId, String prizePlan, Date prizeBegin, Date prizeEnd) {
        // 清空缓存
        cacheMgr.updatePrizeByCache(prizeId);
        // 将db里对应奖品的prize_plan修改为空
        prizeMapper.updatePrizePlanWithTime(prizeId,prizePlan,prizeBegin,prizeEnd);
    }
}

