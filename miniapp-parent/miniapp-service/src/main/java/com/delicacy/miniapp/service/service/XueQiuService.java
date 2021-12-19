package com.delicacy.miniapp.service.service;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:11
 **/
public interface XueQiuService {

    void runAStock();
    void runAStockDesc();
    void runAStockReport();
    void runAStockProfitReport();
    void runAStockCashFlowReport();
    void runAStockBalanceReport();

    void runHKStock();
    void runHKStockReport();
}
