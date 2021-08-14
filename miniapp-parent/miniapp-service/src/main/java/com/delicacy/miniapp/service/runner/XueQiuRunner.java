package com.delicacy.miniapp.service.runner;



import com.delicacy.miniapp.service.pipeline.Map2MongoPipeline;
import com.delicacy.miniapp.service.pipeline.MongoPipeline;
import com.delicacy.miniapp.service.processor.xueqiu.StockProcessor;
import com.delicacy.miniapp.service.processor.xueqiu.StockReportProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

@Component
@Order(value = 1)
@Slf4j
public class XueQiuRunner extends AbstractRunner {

   private static String url = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=CN&type=sh_sz&_=1574236784261";
   private static String url_hk = "https://xueqiu.com/service/v5/stock/screener/quote/list?page=1&size=90&order=desc&orderby=percent&order_by=percent&market=HK&type=hk&_=1574236784261";

    @Override
    public void run(String... args) {
        if(!checkArgs(2, getCommand(this), args)){
            return;
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("cn")) {
            Request request = getRequest(url);
            StockProcessor astockProcessor = new StockProcessor();
            astockProcessor.setSite(getSite("xueqiu.com"));
            dropCollection("xueqiu_astock");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new MongoPipeline(mongoTemplate, "xueqiu_astock","symbol"))
                    .runAsync();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("cn_report")) {
            Request request = getRequest(url);
            StockReportProcessor astockProcessor = new StockReportProcessor();
//            astockProcessor.setAppointReportDates(new String[]{"2021三季报"});
            astockProcessor.setSite(getSite("xueqiu.com"));
//            dropCollection("xueqiu_astock_report");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "xueqiu_astock_report","symbol","report_date"))
                    .runAsync();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("hk")) {
            Request request = getRequest(url_hk);
            StockProcessor astockProcessor = new StockProcessor();
            astockProcessor.setSite(getSite("xueqiu.com"));
            dropCollection("xueqiu_hkstock");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new MongoPipeline(mongoTemplate, "xueqiu_hkstock","symbol"))
                    .runAsync();
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("hk_report")) {
            Request request = getRequest(url_hk);
            StockReportProcessor astockProcessor = new StockReportProcessor();
//            astockProcessor.setAppointReportDates(new String[]{"2021三季报"});
            astockProcessor.setSite(getSite("xueqiu.com"));
//            dropCollection("xueqiu_hkstock_report");
            Spider.create(astockProcessor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "xueqiu_hkstock_report","symbol","report_date"))
                    .runAsync();
        }
    }




}