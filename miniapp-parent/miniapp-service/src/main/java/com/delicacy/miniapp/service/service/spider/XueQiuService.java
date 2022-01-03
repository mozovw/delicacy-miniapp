package com.delicacy.miniapp.service.service.spider;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:11
 **/
public interface XueQiuService {

    void runAStock();
    void runAStockDesc();
    // 综合报表
    void runAStockReport();
    // 利润表
    void runAStockProfitReport();
    // 现金流表
    void runAStockCashFlowReport();
    // 负债表
    void runAStockBalanceReport();
    // 十大股东增减持
    void runAStockTopHolders();
    // 高管增减持
    void runAstockSkHolderChg();

    void runHKStock();
    void runHKStockReport();
}
