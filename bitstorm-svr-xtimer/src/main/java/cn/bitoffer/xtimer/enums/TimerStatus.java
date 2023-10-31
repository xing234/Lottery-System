package cn.bitoffer.xtimer.enums;

public enum TimerStatus {
    Unable(1),
    Enable(2),;

    private TimerStatus(int status) {
        this.status = status;
    }
    private int status;

    public int getStatus() {
        return this.status;
    }
}
