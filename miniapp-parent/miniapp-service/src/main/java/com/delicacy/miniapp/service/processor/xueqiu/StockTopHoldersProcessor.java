package com.delicacy.miniapp.service.processor.xueqiu;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.miniapp.service.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class StockTopHoldersProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/f10/";

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/f10/cn/top_holders.json?symbol=%s&circula=1&count=200"
    };

    static final String URL_PRE_2[] = {
            "https://stock.xueqiu.com/v5/stock/f10/cn/top_holders.json?symbol=%s&locate=%s&start=%s&circula=1"
    };



    @Override
    public void process(Page page) {
        String url = page.getUrl().get();

        if (url.contains(URL_POST)) {

            Map<String, List<String>> stringListMap = HttpUtil.decodeParams(url, "utf-8");
            String symbol = stringListMap.get("symbol").get(0);


            JSONObject jsonObject = page.getJson().toObject(JSONObject.class).getJSONObject("data");
            if (jsonObject == null) {
                return;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            if(jsonArray.size()==0){
                return;
            }
            JSONArray timesJSONArray = jsonObject.getJSONArray("times");
            JSONObject timesJSONObject = timesJSONArray.getJSONObject(0);
            Object reportDate = timesJSONObject.get("name");

            PageProcessor processor = new PageProcessor(page,jsonArray);
            for (int i = 0; i < jsonArray.size(); i++) {
                processor.putmap(i,"symbol", getRealSymbol(symbol));

                if (url.contains("cn")) {
                    processor.putmap(i,"report_date", String.valueOf(reportDate));
                    processor.transfer(i,  "gudongmingcheng","holder_name");
                    processor.transfer(i, "chigushuliang","held_num");
                    processor.transfer(i, "chigubili","held_ratio");
                    processor.transfer(i,  "jiaoshangqibiandong","chg");
                }
            }

            processor.process();


        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            String[] finalUrls = URL_PRE;
            processPage(page,symbol->{
                List<String> collect = new ArrayList<>();
                Arrays.stream(finalUrls).forEach(ee -> {
                    collect.add(String.format(ee, symbol, System.currentTimeMillis()));
                });
                return collect;
            });
        }
    }






}
