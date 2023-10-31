package cn.bitoffer.xtimer.redis;

import cn.bitoffer.xtimer.common.conf.SchedulerAppConf;
import cn.bitoffer.xtimer.model.TaskModel;
import cn.bitoffer.xtimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TaskCache {

    @Autowired
    private RedisBase redisBase;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    SchedulerAppConf schedulerAppConf;

    public String GetTableName(TaskModel taskModel){
        int maxBucket = schedulerAppConf.getBucketsNum();

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(taskModel.getRunTimer());
        long index = taskModel.getTimerId()%maxBucket;
        return sb.append(timeStr).append("_").append(index).toString();
    }

    public boolean cacheSaveTasks(List<TaskModel> taskList){
        try {
            redisTemplate.setEnableTransactionSupport(true);
            redisTemplate.multi();
            for (TaskModel task : taskList) {
                long unix = task.getRunTimer().getTime();
                String tableName = GetTableName(task);
                redisTemplate.opsForZSet().add(
                        tableName,
                        TimerUtils.UnionTimerIDUnix(task.getTimerId(), unix),
                        unix);
            }
            redisTemplate.exec();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
