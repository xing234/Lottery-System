package cn.bitoffer.lottery.model;

import cn.bitoffer.lottery.common.ErrorCode;

public class LotteryResult {
    private ErrorCode errcode;
    LotteryPrizeInfo lotteryPrize;

    public ErrorCode getErrcode() {
        return errcode;
    }

    public void setErrcode(ErrorCode errcode) {
        this.errcode = errcode;
    }

    public LotteryPrizeInfo getLotteryPrize() {
        return lotteryPrize;
    }

    public void setLotteryPrize(LotteryPrizeInfo lotteryPrize) {
        this.lotteryPrize = lotteryPrize;
    }
}
