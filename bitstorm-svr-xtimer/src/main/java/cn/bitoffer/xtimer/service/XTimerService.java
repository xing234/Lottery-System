package cn.bitoffer.xtimer.service;

import cn.bitoffer.xtimer.vo.TimerVO;

import java.util.List;

public interface XTimerService {

    Long CreateTimer(TimerVO timerVO);

    void DeleteTimer(String app, long id);

    void Update(TimerVO timerVO);

    TimerVO GetTimer(String app, long id);

    void EnableTimer(String app, long id);

    void UnEnableTimer(String app, long id);

    List<TimerVO> GetAppTimers(String app);
}
