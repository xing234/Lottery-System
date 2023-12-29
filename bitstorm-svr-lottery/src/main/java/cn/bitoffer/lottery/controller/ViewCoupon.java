package cn.bitoffer.lottery.controller;

import java.util.Date;

public class ViewCoupon {
    private int id;
    private int prizeId;
    private String code;
    private Date sysCreated;
    private Date sysUpdated;
    private int sysStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(int prizeId) {
        this.prizeId = prizeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getSysCreated() {
        return sysCreated;
    }

    public void setSysCreated(Date sysCreated) {
        this.sysCreated = sysCreated;
    }

    public Date getSysUpdated() {
        return sysUpdated;
    }

    public void setSysUpdated(Date sysUpdated) {
        this.sysUpdated = sysUpdated;
    }

    public int getSysStatus() {
        return sysStatus;
    }

    public void setSysStatus(int sysStatus) {
        this.sysStatus = sysStatus;
    }
}
