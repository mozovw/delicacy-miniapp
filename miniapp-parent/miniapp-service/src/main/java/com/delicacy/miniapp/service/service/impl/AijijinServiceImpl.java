package com.delicacy.miniapp.service.service.impl;

import com.delicacy.miniapp.service.processor.aijijin.AiFundPositionProcessor;
import com.delicacy.miniapp.service.processor.aijijin.AiFundRankProcessor;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.AijijinService;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Request;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:41
 **/
@Service
public class AijijinServiceImpl extends AbstractService implements AijijinService {

    static String url = "http://fund.ijijin.cn/data/Net/info/all_F009_desc_0_0_1_9999_0_0_0_jsonp_g.html";

    @Override
    public void runFundRank() {
        Request request = getRequest(url);
        dropCollection("aijijin_fund_rank");
        AiFundRankProcessor processor = new AiFundRankProcessor();
        processor.setSite(getSite("fund.10jqka.com.cn").setTimeOut(10000));
        runSpiderForMap2(request, processor, "aijijin_fund_rank", "symbol");
    }

    @Override
    public void runFundPosition() {
        dropCollection("aijijin_fund_position");
        Request request = getRequest(url);
        AiFundPositionProcessor processor = new AiFundPositionProcessor();
        processor.setSite(getSite("fund.10jqka.com.cn").setTimeOut(10000));
        runSpiderForMap2(request, processor, "aijijin_fund_position", "data_update_time", "fund_code", "gupiaodaima");
    }
}
