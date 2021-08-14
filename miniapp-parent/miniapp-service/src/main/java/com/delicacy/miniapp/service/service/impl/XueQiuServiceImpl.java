package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.processor.xueqiu.StockDescProcessor;
import com.delicacy.miniapp.service.processor.xueqiu.StockProcessor;
import com.delicacy.miniapp.service.processor.xueqiu.StockReportProcessor;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.XueQiuService;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:14
 **/
@Service
public class XueQiuServiceImpl extends AbstractService implements XueQiuService {
     static String URL_ASTOCK = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=CN&type=sh_sz&_=1574236784261";
     static String URL_HKSTOCK = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=HK&type=hk&_=1574236784261";


    @Override
    public void runAStock() {
        Request request = getRequest(URL_ASTOCK);
        StockProcessor processor = new StockProcessor();
        processor.setSite(getSite("xueqiu.com"));
//        dropCollection("xueqiu_astock");
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

        clearBefore4Year("xueqiu_astock_report");

        Request request = getRequest(URL_ASTOCK);
        StockReportProcessor processor = new StockReportProcessor();
        List<String> reportList = getReportList();
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, "xueqiu_astock_report", "symbol", "report_date");
    }

    private void clearBefore4Year(String table) {
        Query query = new Query();
        String[] values = getRemoveReportList().toArray(new String[0]);
        query.addCriteria(
                Criteria.where("report_date").in(values)
        );
        mongoTemplate.remove(query,table);
    }

    private List<String> getRemoveReportList() {
        DateTime offset = DateUtil.offset(DateTime.now(), DateField.YEAR, -4);
        String format = DateUtil.format(offset, "yyyy");
        int year = Integer.parseInt(format);
        List<String> list = new ArrayList<>();
        for (int i = year; i > year-10; i--) {
            list.add( i + "年报");
            list.add( i + "三季报");
            list.add( i + "中报");
            list.add( i + "一季报");
        }
        return list;
    }

    private List<String> getReportList() {
        DateTime now = DateTime.now();
        String format = DateUtil.format(now, "yyyy");
        int year = Integer.parseInt(format);

        List<String> list = null;
        for (int i = year; i > 0; i--) {
            String s = i + "年报";
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i + 1) + "一季报",i  + "三季报");
                break;
            }

            s = i + "三季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, i + "年报");
                break;
            }

            s = i + "中报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, i  + "三季报");
                break;
            }

            s = i + "一季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "年报",i+"二季报");
                break;
            }
        }
        return list;

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
        clearBefore4Year("xueqiu_hkstock_report");
        Request request = getRequest(URL_HKSTOCK);
        StockReportProcessor processor = new StockReportProcessor();
        List<String> reportList = getReportList();
        processor.setAppointReportDates(reportList.toArray(new String[0]));
        processor.setSite(getSite("xueqiu.com"));
        runSpiderForMap2(request, processor, "xueqiu_hkstock_report", "symbol", "report_date");

    }


}
