package cn.bitoffer.xtimer.mapper;


import cn.bitoffer.xtimer.model.TaskModel;
import cn.bitoffer.xtimer.model.TimerModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    /**
     * 批量插入taskModel
     *
     * @param taskList
     */
    void batchSave(@Param("taskList") List<TaskModel> taskList);

    /**
     * 根据timerId删除taskModel
     *
     * @param taskId
     */
    void deleteById(@Param("taskId") Long taskId);

    /**
     * 更新TimerModel
     *
     * @param taskModel
     */
    void update(@Param("taskModel") TaskModel taskModel);

    /**
     * 根据taskId查询Task
     *
     * @param taskId
     * @return TaskModel
     */
    TaskModel getTaskById(@Param("taskId") Long taskId);
}
