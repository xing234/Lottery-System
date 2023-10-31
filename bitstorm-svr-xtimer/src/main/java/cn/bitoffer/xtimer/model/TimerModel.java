package cn.bitoffer.xtimer.model;

import cn.bitoffer.xtimer.common.BaseModel;

import java.io.Serializable;

public class TimerModel extends BaseModel implements Serializable {

    private Long timerId;

    private String app;

    private String name;

    private int status;

    private String cron;

    private String notifyHTTPParam;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getNotifyHTTPParam() {
        return notifyHTTPParam;
    }

    public void setNotifyHTTPParam(String notifyHTTPParam) {
        this.notifyHTTPParam = notifyHTTPParam;
    }
}
