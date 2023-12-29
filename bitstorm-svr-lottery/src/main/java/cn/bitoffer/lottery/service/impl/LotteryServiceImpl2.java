package cn.bitoffer.lottery.service.impl;

import cn.bitoffer.lottery.cache.CacheMgr;
import cn.bitoffer.lottery.common.ErrorCode;
import cn.bitoffer.lottery.constant.Constants;
import cn.bitoffer.lottery.model.*;
import cn.bitoffer.lottery.service.LotteryService;
import cn.bitoffer.lottery.utils.UtilTools;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Service
@Slf4j
public class LotteryServiceImpl2 extends LotteryServiceImpl1 implements LotteryService {

    @Autowired
    protected CacheMgr cacheMgr;
    public LotteryResult lottery(Long userID, String userName, String ip) throws ParseException {
        LotteryResult lotteryResult = new LotteryResult();
        String lockKey = String.format(Constants.lotteryLockKeyPrefix+"%d", userID);
        // 在spring boot 2.0.6版本中整合的redisson,key和锁不能一样
        // redis setnx 操作,此处的lockKey在后面追加1是为了避免redisson锁时报错, 需要和待锁住的数据的key信息不同
        RLock lock = super.redisson.getLock(lockKey);
        lock.lock();
        /*
         ******** 抽奖逻辑*******
         */
        CheckResult checkResult = new CheckResult();
        // 1. 验证用户今日抽奖次数
        if (!checkUserDayLotteryTimesWithCache(userID)) {
            log.info("lotteryV1|CheckUserDayLotteryTimes failed，user_id：{}", userID);
            lotteryResult.setErrcode(ErrorCode.ERR_USER_LIMIT_INVALID);
            return lotteryResult;
        }
        // 2. 验证当天IP参与的抽奖次数
        if (!checkIpLimit(ip)) {
            log.info("lotteryV1|checkIpLimit failed，ip：{}", ip);
            lotteryResult.setErrcode(ErrorCode.ERR_IP_LIMIT_INVALID);
            return lotteryResult;
        }

        // 3. 验证IP是否在ip黑名单
        CheckResult ipCheckResult = checkBlackIpWithCache(ip);
        checkResult.setBlackIp(ipCheckResult.getBlackIp());
        if (!ipCheckResult.isOk()) {
            log.info("lotteryV1|checkBlackIp failed，ip：{}", ip);
            lotteryResult.setErrcode(ErrorCode.ERR_BLACK_IP);
            return lotteryResult;
        }

        // 4. 验证用户是否在用户黑名单
        CheckResult userCheckResult = checkBlackUserWithCache(userID);
        checkResult.setBlackUser(userCheckResult.getBlackUser());
        if (!userCheckResult.isOk()) {
            log.info("lotteryV1|checkBlackUser failed，user_id：{}", userID);
            lotteryResult.setErrcode(ErrorCode.ERR_BLACK_USER);
            return lotteryResult;
        }
        checkResult.setOk(true);

        // 5. 奖品匹配
        int prizeCode = UtilTools.getRandom(Constants.prizeCodeMax);
        LotteryPrizeInfo prize = getPrizeWithCache(prizeCode);
        if (prize == null)  {
            log.info("lotteryV1|getPrize null");
            lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
            return lotteryResult;
        }

        if (prize.getPrizeNum() <=0) {
            log.info("lotteryV1|prize_num invalid,prize_id: {}", prize.getId());
            lotteryResult.setErrcode(ErrorCode.ERR_NOT_WON);
            return lotteryResult;
        }

        // 6. 剩余奖品发放
        if (!giveOutPrizeWithCache(prize.getId())) {
            log.info("lotteryV1|prize not enough,prize_id: {}", prize.getId());
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


    public boolean checkUserDayLotteryTimesWithCache(Long userId){
        int userLotteryNum = cacheMgr.incrUserDayLotteryNum(userId);
        log.info("checkUserDayLotteryTimesWithCache|uid={}|userLotteryNum={}", userId, userLotteryNum);
        if (userLotteryNum > Constants.userPrizeMax) {
            return false; // 缓存验证没通过，直接返回
        }
        // 通过数据库验证，还要在数据库中做一次验证
        LotteryTimes userLotteryTimes = getUserCurrentLotteryTimes(userId);
        if (userLotteryTimes != null) {
            // 数据库验证今天的抽奖记录已经达到了抽奖次数限制，不能在抽奖
            if (userLotteryTimes.getNum() > Constants.userPrizeMax){
                // 缓存数据不可靠，不对，需要更新
                if (userLotteryTimes.getNum() > userLotteryNum) {
                    if (!cacheMgr.initUserLuckyNum(userId,userLotteryTimes.getNum())){
                        log.error("checkUserDayLotteryTimesWithCache|initUserLuckyNum error");
                    }
                }
                log.info("checkUserDayLotteryTimes|user_id: {}, lottery times: {}",userId,userLotteryTimes.getNum());
                return false;
            }else{
                int num = userLotteryTimes.getNum() + 1;
                userLotteryTimes.setNum(num);
                // 此时次数抽奖次数增加了，需要更新缓存
                if (userLotteryTimes.getNum() > userLotteryNum) {
                    // 缓存更新失败
                    if (!cacheMgr.initUserLuckyNum(userId,userLotteryTimes.getNum())){
                        log.error("checkUserDayLotteryTimesWithCache|initUserLuckyNum error");
                        return false;
                    }
                }
                // 更新数据库
                super.lotteryTimesMapper.update(userLotteryTimes);
            }
            return true;
        }
        // 新增一条抽奖记录
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR);  // 获取当前年
        int m  = calendar.get(Calendar.MONTH) + 1; // 获取当前月
        int d = calendar.get(Calendar.DATE); // 获取当前日
        String strDay = String.format("%d%02d%02d", y, m, d);
        int day = Integer.parseInt(strDay);
        userLotteryTimes = new LotteryTimes();
        userLotteryTimes.setUserId(userId);
        userLotteryTimes.setDay(new Long(day));
        userLotteryTimes.setNum(1);
        userLotteryTimes.setSysCreated(calendar.getTime());
        userLotteryTimes.setSysUpdated(calendar.getTime());
        lotteryTimesMapper.save(userLotteryTimes);
        // 同步到缓存
        if (!cacheMgr.initUserLuckyNum(userId,userLotteryTimes.getNum())){
            log.error("checkUserDayLotteryTimesWithCache|initUserLuckyNum error");
            return false;
        }
        return true;
    }

    public CheckResult checkBlackIpWithCache(String ipStr) throws ParseException {
        CheckResult checkResult = new CheckResult();
        BlackIp blackIp = getBLackIpWithCache(ipStr);
        checkResult.setBlackIp(blackIp);
        if (blackIp == null || !blackIp.getIp().isEmpty()) {
            checkResult.setOk(true);
            return checkResult;
        }
        Date data = new Date();
        if (data.before(blackIp.getBlackTime())) {
            checkResult.setOk(false);
            return checkResult;
        }
        checkResult.setOk(true);
        return checkResult;
    }

    public BlackIp getBLackIpWithCache(String ip) throws ParseException {
        BlackIp blackIp = cacheMgr.getBlackIpByCache(ip);
        // 从缓存获取到数据
        if (blackIp != null) {
            return blackIp;
        }
        // 缓存没有获取到，从db获取
        blackIp = blackIpMapper.getByIP(ip);
        cacheMgr.setBlackIpByCache(blackIp);
        return blackIp;
    }


    public CheckResult checkBlackUserWithCache(Long userId) throws ParseException {
        CheckResult checkResult = new CheckResult();
        BlackUser blackUser = getBlackUserWithCache(userId);
        checkResult.setBlackUser(blackUser);
        Date date = new Date();
        if (blackUser != null && date.before(blackUser.getBlackTime())) {
            checkResult.setOk(false);
        }else{
            checkResult.setOk(true);
        }
        return checkResult;
    }

    public BlackUser getBlackUserWithCache(Long userId) throws ParseException {
        BlackUser blackUser = cacheMgr.getBlackUserByCache(userId);
        if (blackUser != null){
            return blackUser;
        }
        blackUser = blackUserMapper.getByUserId(userId);
        cacheMgr.setBlackUserByCache(blackUser);
        return blackUser;
    }

    public LotteryPrizeInfo getPrizeWithCache(int prizeCode) throws ParseException {
        ArrayList<LotteryPrizeInfo> lotteryPrizeInfoList = getAllUsefulPrizesWithCache();
        for (LotteryPrizeInfo lotteryPrizeInfo : lotteryPrizeInfoList) {
            if (lotteryPrizeInfo.getPrizeCodeLow() <= prizeCode && lotteryPrizeInfo.getPrizeCodeHigh() >= prizeCode) {
                return lotteryPrizeInfo;
            }
        }
        return null;
    }

    public ArrayList<LotteryPrizeInfo> getAllUsefulPrizesWithCache() throws ParseException {
        ArrayList<Prize> prizeList = getAllUsefulPrizeListWithCache();
        if (prizeList == null || prizeList.isEmpty()) {
            return null;
        }
        ArrayList<LotteryPrizeInfo> lotteryPrizeInfoList = new ArrayList<LotteryPrizeInfo>();
        for (Prize prize : prizeList){
            String[] codes = prize.getPrizeCode().split("-");
            if (codes.length == 2) {
                String codeA = codes[0];
                String codeB = codes[1];
                int low = Integer.parseInt(codeA);
                int high = Integer.parseInt(codeB);
                if (high >= low && low >= 0 && high < Constants.prizeCodeMax) {
                    LotteryPrizeInfo lotteryPrizeInfo = new LotteryPrizeInfo();
                    lotteryPrizeInfo.setId(prize.getId());
                    lotteryPrizeInfo.setTitle(prize.getTitle());
                    lotteryPrizeInfo.setPrizeNum(prize.getPrizeNum());
                    lotteryPrizeInfo.setLeftNum(prize.getLeftNum());
                    lotteryPrizeInfo.setPrizeCodeLow(low);
                    lotteryPrizeInfo.setPrizeCodeHigh(high);
                    lotteryPrizeInfo.setImg(prize.getImg());
                    lotteryPrizeInfo.setDisplayOrder(prize.getDisplayOrder());
                    lotteryPrizeInfo.setPrizeType(prize.getPrizeType());
                    lotteryPrizeInfo.setPrizeProfile(prize.getPrizeProfile());
                    lotteryPrizeInfoList.add(lotteryPrizeInfo);
                }
            }
        }
        return lotteryPrizeInfoList;
    }

    public ArrayList<Prize> getAllUsefulPrizeListWithCache() throws ParseException {
        ArrayList<Prize> prizeList = getAllPrizeListWithCache();
        Date now = new Date();
        ArrayList<Prize> usefulPrizeList = new ArrayList<Prize>();
        for (Prize prize : prizeList) {
            if (prize.getId() > 0 && prize.getSysStatus() == 1 && prize.getPrizeNum() > 0 &&
            prize.getPrizeBegin().before(now) && prize.getEndTime().after(now)) {
                usefulPrizeList.add(prize);
            }
        }
        return usefulPrizeList;
    }


    public ArrayList<Prize> getAllPrizeListWithCache() throws ParseException {
        ArrayList<Prize> prizeList = cacheMgr.getAllPrizesByCache();
        if (prizeList == null || prizeList.isEmpty()) {
            // 缓存没查到，从db获取
            prizeList = prizeMapper.getAll();
            // 从db获取到数据
            cacheMgr.setAllPrizesByCache(prizeList);
        }
        return prizeList;
    }

    public boolean giveOutPrizeWithCache(Long prizeId) {
        int ret = prizeMapper.decrLeftNum(prizeId,1);
        if (ret <= 0){
            return false;
        }
        cacheMgr.updatePrizeByCache(prizeId);
        return true;
    }

    // 带缓存的优惠券发奖，从缓存中拿出一个优惠券,要用缓存的话，需要项目启动的时候将优惠券导入到缓存
    public String prizeCodeDiffWithCache(Long prizeId) {
        String code = cacheMgr.getNextUsefulCouponByCache(prizeId);
        if (code.isEmpty()) {
            return "";
        }
        // 缓存中能够取到优惠券，在去更改db
        couponMapper.updateByCode(code);
        return code;
    }

    public Prize getPrizeWithCache(Long prizeId) throws ParseException {
        ArrayList<Prize> prizeList = getAllPrizeListWithCache();
        for(Prize prize: prizeList){
            if (prize.getId() == prizeId) {
                return prize;
            }
        }
        return null;
    }

    public void importCouponWithCache(Long prizeId, String codes) throws ParseException {
        if(prizeId <= 0){
            return;
        }
        Prize prize = getPrizeWithCache(prizeId);
        if (prize == null || prize.getPrizeType() != Constants.prizeTypeCouponDiff) {
            log.info("invalid prize type: {} or id: {}",prize.getPrizeType(),prizeId);
        }
        int successNum=0,failNum = 0;
        String[] codeList = Strings.split(codes, '\n');
        for(String code:codeList){
            code = code.trim();
            Coupon coupon = new Coupon();
            coupon.setPrizeId(prizeId);
            coupon.setCode(code);
            coupon.setSysStatus(1);
            int ret = couponMapper.save(coupon);
            if (ret == 0) {
                failNum++;
            }else{
                boolean ok = cacheMgr.importCouponByCache(prizeId,code);
                if(!ok){
                    failNum++;
                }else {
                    successNum++;
                }
            }
        }
        log.info("importCouponWithCache successNum: {}, failNum: {}",successNum,failNum);
    }
}
