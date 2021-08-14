package com.delicacy.miniapp.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import com.delicacy.miniapp.service.service.FinanceComprehensiveService;
import com.delicacy.miniapp.service.service.FinanceFundSelectionService;
import com.delicacy.miniapp.service.service.FinanceValuationService;
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
    private FinanceComprehensiveService financeComprehensiveService;

    @Autowired
    private FinanceFundSelectionService financeFundSelectionService;

    @Autowired
    private FinanceValuationService financeValuationService;

    @Scheduled(fixedRate = Integer.MAX_VALUE)
    public void start(){
//        financeValuationService.runValuation();
        financeComprehensiveService.runComprehensive();
//        financeFundSelectionService.runFundSelection();
    }


    @Scheduled(cron = "0 0 20 * * ? ")
    public void startComprehensive() {
        startAtWeekDay(e -> financeComprehensiveService.runComprehensive());
    }

    @Scheduled(cron = "0 0 18 * * ? ")
    public void startFundSelection() {
        startAtWeekDay(e -> financeFundSelectionService.runFundSelection());
    }

    @Scheduled(cron = "0 0 5 * * ? ")
    public void startValuation() {
        startAtSUNDAY(e -> financeValuationService.runValuation());
    }

    private void randomTime() {
        try {
            int x = (int) (Math.random() * 3600 + 1);
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
