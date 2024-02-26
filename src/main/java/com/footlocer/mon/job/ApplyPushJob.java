package com.footlocer.mon.job;

import com.footlocer.mon.manager.TouchUpdateService;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
public class ApplyPushJob {

    @Autowired
    private TouchUpdateService touchUpdateService;

    @Scheduled(cron = "0 */30 * * * ?")
    public void syncProductApply() throws IOException, ApiException {
        log.info("====================== 开始touch 改价 ======================");
        touchUpdateService.checkGuaShouList();
    }



}
