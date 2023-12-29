package cn.bitoffer.lottery.utils;

import java.util.Random;

public class UtilTools {
    public static int getRandom(int maxValue) {
        Random random = new Random();
        //设置随机数种子
        random.setSeed(System.currentTimeMillis());
        return random.nextInt(maxValue);
    }
}
