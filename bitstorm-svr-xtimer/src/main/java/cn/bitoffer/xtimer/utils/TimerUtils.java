package cn.bitoffer.xtimer.utils;

import org.quartz.CronExpression;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class TimerUtils {
    public static String GetCreateLockKey(String app){
        return "create_timer_lock_"+app;
    }

    public static String GetEnableLockKey(String app){
        return "enable_timer_lock_"+app;
    }

     public static String GetTokenStr() {
        long timestamp = System.currentTimeMillis();
        String thread = Thread.currentThread().getName();
        return thread+timestamp;
    }

    public static Date GetForwardTwoMigrateStepEnd(Date start, int diffMinutes){
        Date end = new Date(start.getTime() + 2L *diffMinutes * 60000);
        return end;
    }

    public static List<Date> GetCronNextsBetween(CronExpression cronExpression, Date now, Date end){
        List<Date> times = new ArrayList<>();
        if( end.before(now)){
            return times;
        }

        for (Date start =now;start.before(end);){
            Date next = cronExpression.getNextValidTimeAfter(start);
            times.add(next);
            start = next;
        }
        return times;
    }

    public static String UnionTimerIDUnix(long timerId, long unix){
        return new StringBuilder().append(timerId).append("_").append(unix).toString();
    }
}
