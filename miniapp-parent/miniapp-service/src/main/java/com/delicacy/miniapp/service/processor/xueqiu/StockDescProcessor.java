package com.delicacy.miniapp.service.processor.xueqiu;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.miniapp.service.processor.AbstactProcessor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class StockDescProcessor extends AbstactProcessor {


    public static final String URL_POST = "https://xueqiu.com/stock/industry/stockList.json";

    public static final String URL_PRE = "https://xueqiu.com/stock/industry/stockList.json?code=%s&type=1&size=1";

    @Override
    public void process(Page page) {
        if (page.getUrl().get().contains(URL_POST)) {
            JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
            Object platename = jsonObject.get("platename");
            if (ObjectUtil.isEmpty(platename)){
                return;
            }
            PageProcessor pageProcessor = new PageProcessor(page, jsonObject);
            pageProcessor.getPage().putField("symbol",getRealSymbol(jsonObject.get("code").toString()));
            pageProcessor.transfer("platename", "platename");

        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            processPage(page,symbol->{
                return Collections.singletonList(String.format(URL_PRE, symbol));
            });
        }
    }


}
