package com.footlocer.mon.manager;


import com.footlocer.mon.config.SeatMonitorProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
//@EnableScheduling
public class SeatMonitorScheduler {

    private final SeatMonitorService service;
    private final SeatMonitorProperties props;

    public SeatMonitorScheduler(SeatMonitorService service, SeatMonitorProperties props) {
        this.service = service;
        this.props = props;
    }

    // 如不需要定时器，删除本类或注释掉 @Component/@EnableScheduling 即可
    @Scheduled(fixedDelayString = "#{${seat.monitor.periodSec:30} * 1000}")
    public void tick() {
        //service.pollOnce();
    }
}

