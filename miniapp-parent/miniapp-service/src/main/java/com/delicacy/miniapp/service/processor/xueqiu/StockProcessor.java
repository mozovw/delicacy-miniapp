package com.delicacy.miniapp.service.processor.xueqiu;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.miniapp.service.processor.AbstactProcessor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.cache.CacheManager;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class StockProcessor extends AbstactProcessor {


    public static final String URL_POST = "https://stock.xueqiu.com/v5/stock/quote.json";
    public static final String URL_POST_2 = "https://xueqiu.com/stock/industry/stockList.json?code=%s&type=1&size=1";

    public static final String URL_PRE = "https://stock.xueqiu.com/v5/stock/quote.json?symbol=%s&extend=detail";

    volatile boolean flag = false;

    @Override
    public void process(Page page) {
        if (page.getUrl().get().contains(URL_POST)) {
            JSONObject jsonObject = page.getJson().toObject(JSONObject.class).getJSONObject("data").getJSONObject("quote");
            PageProcessor pageProcessor = new PageProcessor(page, jsonObject);
            pageProcessor.transfer("symbol", "symbol");
            pageProcessor.transfer("name", "name");
            pageProcessor.transfer("current", "current");
            pageProcessor.transfer("zongshizhi", "market_capital");
            pageProcessor.transfer("zongguben", "total_shares");
            pageProcessor.transfer("liutongzhi", "float_market_capital");
            pageProcessor.transfer("liutonggu", "float_shares");
            pageProcessor.transfer("shiyinglv_dong", "pe_forecast");
            pageProcessor.transfer("shiyinglv_TTM", "pe_ttm");
            pageProcessor.transfer("shiyinglv_jing", "pe_lyr");
            pageProcessor.transfer("guxi_TTM", "dividend");
            pageProcessor.transfer("guxilv_TTM", "dividend_yield");
            pageProcessor.transfer("shijinglv", "pb");
            pageProcessor.transfer("meigujingzichan", "navps");
            pageProcessor.transfer("meigushouyi", "eps");
            pageProcessor.transfer("52zhouzuigao", "high52w");
            pageProcessor.transfer("52zhouzuidi", "low52w");
            pageProcessor.transfer("huobidanwei", "currency");

        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            processPage(page,symbol->{
                return Collections.singletonList(String.format(URL_PRE, symbol));
            });
        }
    }




}
