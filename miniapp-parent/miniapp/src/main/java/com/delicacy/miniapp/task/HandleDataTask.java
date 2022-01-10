package com.delicacy.miniapp.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import com.delicacy.miniapp.service.service.finance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:47
 **/
@Component
public class HandleDataTask {
    @Autowired
    private ComprehensiveService comprehensiveService;

    @Autowired
    private PegService pegService;

    @Autowired
    private TopHoldersSerivice topHolderSerivice;

    @Autowired
    private SkHolderSerivice skHolderSerivice;

    @Autowired
    private ValuationService valuationService;

    @Scheduled(fixedRate = Integer.MAX_VALUE)
    public void start() {
//        valuationService.runTask();
//        comprehensiveService.runTask();
//        pegService.runTask();
//        topHolderSerivice.runTask();
//        skHolderSerivice.runTask();
    }


    //    @Scheduled(cron = "0 0 20 * * ? ")
    public void startComprehensive() {
        startAtWeekDay(e -> comprehensiveService.runTask());
    }

    //    @Scheduled(cron = "0 0 20 * * ? ")
    public void startValuation() {
        startAtWeekDay(e -> valuationService.runTask());
    }

    private void randomTime() {
        try {
            int x = (int) (Math.random() * 1000 + 1);
            TimeUnit.SECONDS.sleep(x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startAtSUNDAY(Consumer consumer) {
        Week week = DateUtil.dayOfWeekEnum(DateTime.now());
        if (week.equals(Week.SUNDAY)) {
            randomTime();
            consumer.accept(null);
        }
    }

    private void startAtWeekDay(Consumer consumer) {
        if (!DateUtil.isWeekend(DateTime.now())) {
            randomTime();
            consumer.accept(null);
        }
    }
}
