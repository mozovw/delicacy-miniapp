package com.delicacy.miniapp.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import com.delicacy.miniapp.service.service.spider.AijijinService;
import com.delicacy.miniapp.service.service.spider.JinrongcaifuService;
import com.delicacy.miniapp.service.service.spider.XueQiuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:47
 **/
@EnableScheduling
@Component
public class CollectDataTask {
    @Autowired
    private AijijinService aijijinService;

    @Autowired
    private XueQiuService xueQiuService;

    @Autowired
    private JinrongcaifuService jinrongcaifuService;


    @Scheduled(fixedRate = Integer.MAX_VALUE)
    public void start(){
//        new Thread(()-> aijijinService.runFundRank()).start();
//        new Thread(()-> aijijinService.runFundPosition()).start();


        new Thread(()-> {
//            xueQiuService.runAStock();
//            xueQiuService.runAStockReport();
//            xueQiuService.runAStockDesc();
            xueQiuService.runAStockProfitReport();
//            xueQiuService.runAStockCashFlowReport();
            xueQiuService.runAStockBalanceReport();
//            xueQiuService.runAStockTopHolders();
//            xueQiuService.runAstockSkHolderChg();
//            jinrongcaifuService.runForcastReport();
        }).start();

    }

    // 工作日16点执行
//    @Scheduled(cron = "0 0 15 * * ? ")
    public void startFundRank() {
        startAtWeekDay(e -> aijijinService.runFundRank());
    }

    // 每周5 2点执行一次
//    @Scheduled(cron = "0 0 15 * * ?")
    public void startFundPosition() {
        startAtFRIDAY(e -> aijijinService.runFundPosition());
    }

//    @Scheduled(cron = "0 0 15 * * ? ")
    public void startAStock() {
        startAtWeekDay(e -> xueQiuService.runAStock());
    }

//    @Scheduled(cron = "0 0 15 1 * ? ")
    public void startAStockDesc() {
         xueQiuService.runAStockDesc();
    }

//    @Scheduled(cron = "0 0 15 * * ? ")
    public void startHKStock() {
        startAtWeekDay(e -> xueQiuService.runHKStock());
    }

//    @Scheduled(cron = "0 0 17 * 1-4,7-8,10,11 ?")
    public void startAStockReport() {
        startAtWeekDay(e -> xueQiuService.runAStockReport());
    }

    // 每周六2点执行一次
//    @Scheduled(cron = "0 0 2 * * ?")
    public void startHKStockReport() {
        startAtSATURDAY(e -> xueQiuService.runHKStockReport());

    }

    private void randomTime() {
        try {
            int x = (int) (Math.random() * 1000 + 1);
            TimeUnit.SECONDS.sleep(x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void startAtWeekDay(Consumer consumer) {
        if (!DateUtil.isWeekend(DateTime.now())) {
            randomTime();
            consumer.accept(null);
        }
    }
    private void startAtSATURDAY(Consumer consumer) {
        Week week = DateUtil.dayOfWeekEnum(DateTime.now());
        if (week.equals(Week.SATURDAY)) {
            randomTime();
            consumer.accept(null);
        }
    }

    private void startAtFRIDAY(Consumer consumer) {
        Week week = DateUtil.dayOfWeekEnum(DateTime.now());
        if (week.equals(Week.FRIDAY)) {
            randomTime();
            consumer.accept(null);
        }
    }

}
