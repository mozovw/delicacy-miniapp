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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class StockDescProcessor extends AbstactProcessor {


    public static final String URL_POST = "https://xueqiu.com/stock/industry/stockList.json";

    public static final String URL_PRE = "https://xueqiu.com/stock/industry/stockList.json?code=%s&type=1&size=1";

    volatile boolean flag = false;


    @Override
    public void process(Page page) {
        if (page.getUrl().get().contains(URL_POST)) {
            JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
            Object platename = jsonObject.get("platename");
            if (ObjectUtil.isEmpty(platename))return;
            PageProcessor pageProcessor = new PageProcessor(page, jsonObject);
            pageProcessor.transfer("symbol", "code");
            pageProcessor.transfer("platename", "platename");

        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            if (flag) {
                return;
            }
            String rawText = page.getRawText();
            JSONObject jsonObject = JSON.parseObject(rawText);
            rawText = jsonObject.get("data").toString();
            jsonObject = JSON.parseObject(rawText);
            rawText = jsonObject.get("list").toString();
            JSONArray jsonArray = JSON.parseArray(rawText);
            List<String> collect = jsonArray.stream().map(e -> {
                JSONObject e1 = (JSONObject) e;
                String symbol = e1.get("symbol").toString();
                return String.format(URL_PRE, symbol);
            }).collect(Collectors.toList());
            page.addTargetRequests(collect);
            //todo update flag
            Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
            long longPage = Long.parseLong(stringListMap.get("page").get(0));
            long sum = longPage * Long.parseLong(stringListMap.get("size").get(0));
            long count = Long.parseLong(jsonObject.get("count").toString());
            flag = count < sum;
            //todo update page
            //todo get newurl
            stringListMap.put("page", Lists.newArrayList(String.valueOf(longPage + 1)));
            String params = HttpUtil.toParams(stringListMap);
            String string = page.getUrl().toString();
            String newUrl = string.substring(0, string.indexOf("?") + 1) + params;
            page.addTargetRequest(newUrl);
        }
    }


}
