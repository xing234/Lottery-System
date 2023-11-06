package cn.bitoffer.xtimer.vo;

import cn.bitoffer.xtimer.dto.NotifyHTTPParam;
import cn.bitoffer.xtimer.enums.TimerStatus;
import cn.bitoffer.xtimer.model.TimerModel;
import cn.bitoffer.xtimer.utils.JSONUtil;
import org.springframework.beans.BeanUtils;

import java.util.List;

public class TimerVO {
    /**
     * 定时任务ID
     */
    private Long timerId;

    /**
     * APP名称（所属业务）
     */
    private String app;

    /**
     * 定时任务-名称
     */
    private String name;

    /**
     * 定时任务-状态
     */
    private TimerStatus status;

    /**
     *  定时任务-定时配置
     */
    private String cron;

    /**
     * Name 定时任务-回调参数配置
     */
    private NotifyHTTPParam notifyHTTPParam;

    /**
     * 包装类转对象
     *
     * @param timerVO
     * @return
     */
    public static TimerModel voToObj(TimerVO timerVO) {
        if (timerVO == null) {
            return null;
        }
        TimerModel timerModel = new TimerModel();
        timerModel.setApp(timerVO.getApp());
        timerModel.setTimerId(timerVO.getTimerId());
        timerModel.setName(timerVO.getName());
        timerModel.setStatus(timerVO.getStatus().getStatus());
        timerModel.setCron(timerVO.getCron());
        timerModel.setNotifyHTTPParam(JSONUtil.toJsonString(timerVO.notifyHTTPParam));
        return timerModel;
    }

    /**
     * 对象转包装类
     *
     * @param timerModel
     * @return
     */
    public static TimerVO objToVo(TimerModel timerModel) {
        if (timerModel == null) {
            return null;
        }
        TimerVO timerVO = new TimerVO();
        timerVO.setApp(timerModel.getApp());
        timerVO.setTimerId(timerModel.getTimerId());
        timerVO.setName(timerModel.getName());
        timerVO.setStatus(TimerStatus.getTimerStatus(timerModel.getStatus()));
        timerVO.setCron(timerModel.getCron());

        NotifyHTTPParam httpParam = JSONUtil.parseObject(timerModel.getNotifyHTTPParam(),NotifyHTTPParam.class);
        timerVO.setNotifyHTTPParam(httpParam);

        BeanUtils.copyProperties(timerModel, timerVO);
        return timerVO;
    }

    public Long getTimerId() {
        return timerId;
    }

    public void setTimerId(Long timerId) {
        this.timerId = timerId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimerStatus getStatus() {
        return status;
    }

    public void setStatus(TimerStatus status) {
        this.status = status;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public NotifyHTTPParam getNotifyHTTPParam() {
        return notifyHTTPParam;
    }

    public void setNotifyHTTPParam(NotifyHTTPParam notifyHTTPParam) {
        this.notifyHTTPParam = notifyHTTPParam;
    }
}
