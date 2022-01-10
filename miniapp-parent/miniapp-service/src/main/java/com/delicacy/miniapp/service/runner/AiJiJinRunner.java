package com.delicacy.miniapp.service.runner;


import com.delicacy.miniapp.service.pipeline.Map2MongoPipeline;
import com.delicacy.miniapp.service.processor.aijijin.AiFundPositionProcessor;
import com.delicacy.miniapp.service.processor.aijijin.AiFundRankProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

@Component
@Order(value = 1)
@Slf4j
public class AiJiJinRunner extends AbstractRunner {
    String url = "http://fund.ijijin.cn/data/Net/info/all_F009_desc_0_0_1_9999_0_0_0_jsonp_g.html";


    @Override
    public void run(String... args) {
        if (!checkArgs(2, getCommand(this), args)) {
            return;
        }

        Request request = getRequest(url);
        if ("fundrank".equalsIgnoreCase(String.valueOf(args[1]))) {
            dropCollection("fund_rank");
            AiFundRankProcessor processor = new AiFundRankProcessor();
            processor.setSite(getSite("fund.10jqka.com.cn").setTimeOut(10000));
            Spider.create(processor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new ConsolePipeline())
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "aijijin_fund_rank","symbol"))
                    .run();
        }
        if ("fundposition".equalsIgnoreCase(String.valueOf(args[1]))) {
            AiFundPositionProcessor processor = new AiFundPositionProcessor();
            processor.setSite(getSite("fund.10jqka.com.cn").setTimeOut(10000));
            dropCollection("fund_position");
            Spider.create(processor)
                    .thread(threadNum)
                    .addRequest(request)
                    .addPipeline(new ConsolePipeline())
                    .addPipeline(new Map2MongoPipeline(mongoTemplate, "aijijin_fund_position","data_update_time","fund_code","symbol"))
                    .run();
        }

    }


}