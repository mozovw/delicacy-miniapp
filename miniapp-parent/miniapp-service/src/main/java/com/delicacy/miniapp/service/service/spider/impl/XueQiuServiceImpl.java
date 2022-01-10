package com.delicacy.miniapp.service.service.spider.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.processor.xueqiu.*;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import com.delicacy.miniapp.service.service.spider.XueQiuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Request;

import java.util.List;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:14
 **/
@Service
public class XueQiuServiceImpl extends AbstractService implements XueQiuService {
    static String URL_ASTOCK = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=CN&type=sh_sz&_=1574236784261";
    static String URL_HKSTOCK = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=HK&type=hk&_=1574236784261";

    @Autowired
    private BaseDataService baseDataService;

    @Override
    public void runAStock() {
        final List<String> symbols = baseDataService.allPrefixSymbol();
        Request request = getRequest(URL_ASTOCK);
        StockProcessor processor = new StockProcessor();
        processor.setSymbols(symbols);
        processor.setSite(getSite("xueqiu.com"));
        runSpider(request, processor, "xueqiu_astock", "symbol");
    }

    @Override
    public void runAStockDesc() {
        Request request = getRequest(URL_ASTOCK);
        StockDescProcessor processor = new StockDescProcessor();
        processor.setSite(getSite("xueqiu.com"));
//        dropCollection("xueqiu_astock_desc");
        runSpider(request, processor, "xueqiu_astock_desc", "symbol");
    }

    @Override
    public void runAStockReport() {
        String collection = "xueqiu_astock_report";
        clearBefore4Year(collection);

        Request request = getRequest(URL_ASTOCK);
        StockReportProcessor processor = new StockReportProcessor();
        List<String> reportList = getReportList(collection, -4);
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, collection, "symbol", "report_date");
    }

    @Override
    public void runAStockProfitReport() {
        String collection = "xueqiu_astock_profit_report";

        clearBefore4Year(collection);
        Request request = getRequest(URL_ASTOCK);
        StockProfitReportProcessor processor = new StockProfitReportProcessor();
        List<String> reportList = getReportList(collection, -5);
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, collection, "symbol", "report_date");
    }

    @Override
    public void runAStockCashFlowReport() {
        String collection = "xueqiu_astock_cash_flow_report";

        clearBefore4Year(collection);
        Request request = getRequest(URL_ASTOCK);
        StockCashFlowReportProcessor processor = new StockCashFlowReportProcessor();
        List<String> reportList = getReportList(collection, -5);
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, collection, "symbol", "report_date");
    }

    @Override
    public void runAStockBalanceReport() {
        String collection = "xueqiu_astock_balance_report";

        clearBefore4Year(collection);
        Request request = getRequest(URL_ASTOCK);
        StockBalanceReportProcessor processor = new StockBalanceReportProcessor();
        List<String> reportList = getReportList(collection, -5);
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, collection, "symbol", "report_date");
    }

    @Override
    public void runAStockTopHolders() {
        String collection = "xueqiu_astock_top_holders";

        clearBeforeNumYear(collection, -2);
        Request request = getRequest(URL_ASTOCK);
        StockTopHoldersProcessor processor = new StockTopHoldersProcessor();
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, collection, "symbol", "report_date", "gudongmingcheng");
    }

    @Override
    public void runAstockSkHolderChg() {
        String collection = "xueqiu_astock_sk_holder_chg";

        DateTime offset = DateTime.now();
        final DateTime dateTime = DateUtil.offsetMonth(offset, -6);
        final String formatDate = DateUtil.formatDate(dateTime);
        Query query = new Query();
        query.addCriteria(
                Criteria.where("biandongriqi").lt(formatDate)
        );
        mongoTemplate.remove(query, collection);
        Request request = getRequest(URL_ASTOCK);
        StockSkHolderChgProcessor processor = new StockSkHolderChgProcessor();
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, collection, "symbol", "biandongriqi", "mingcheng");
    }


    @Override
    public void runHKStock() {
        Request request = getRequest(URL_HKSTOCK);
        StockProcessor processor = new StockProcessor();
        processor.setSite(getSite("xueqiu.com"));
//        dropCollection("xueqiu_hkstock");
        runSpider(request, processor, "xueqiu_hkstock", "symbol");
    }

    @Override
    public void runHKStockReport() {
        String collection = "xueqiu_hkstock_report";

        clearBefore4Year(collection);
        Request request = getRequest(URL_HKSTOCK);
        StockReportProcessor processor = new StockReportProcessor();
        List<String> reportList = getReportList(collection, -4);
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, "xueqiu_hkstock_report", "symbol", "report_date");

    }


}
