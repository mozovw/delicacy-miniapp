package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.processor.aijijin.AiFundRankProcessor;
import com.delicacy.miniapp.service.processor.jinrongcaifu.CaifuForcastReportProcessor;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.JinrongcaifuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Request;

import java.util.Date;
import java.util.List;

/**
 * @author yutao
 * @create 2021-12-26 15:44
 **/
@Slf4j
@Service
public class JinrongcaifuServiceImpl extends AbstractService implements JinrongcaifuService {
    String url ="https://datacenter-web.eastmoney.com/securities/api/data/v1/get?" +
            "sortColumns=NOTICE_DATE%2CSECURITY_CODE&sortTypes=-1%2C-1&pageSize=20000&pageNumber=1&reportName=RPT_PUBLIC_OP_NEWPREDICT&" +
            "columns=ALL&token=894050c76af8597a853f5b408b759f5d&filter=(REPORT_DATE%3D%27{1}%27)";
    @Override
    public void runForcastReport() {

        String collection = "jinrongcaifu_forcast_report";
        clearBeforeNumYear(collection,-2);

        List<Date> lastReportDateList = getLastReportDateList();
        lastReportDateList.forEach(e->{
            String s = DateUtil.formatDate(e);
            String format = url.replace("{1}",s);
            Request request = getRequest(format);
            CaifuForcastReportProcessor processor = new CaifuForcastReportProcessor();
            processor.setSite(getSite("data.eastmoney.com").setTimeOut(10000));
            runSpiderForMap2(request, processor, collection, "symbol");
        });
    }
}
