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

@Slf4j
public class StockSkHolderChgProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/f10/";


    static final String URL_PRE_HK[] = {

    };

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/f10/cn/skholderchg.json?symbol=%s&extend=true&page=1&size=20"
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
            PageProcessor processor = new PageProcessor(page,jsonArray);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonArrayJSONObject = jsonArray.getJSONObject(i);
                processor.putmap(i,"symbol", symbol.replace("SH", "").replace("SZ", ""));
                if (url.contains("cn")) {
                    processor.transfer(i, "gongsi","name");
                    processor.transfer(i, "mingcheng","manage_name");
                    processor.transfer(i, "zhiwei","duty");
                    processor.getmap(i).put("biandongriqi",jsonArrayJSONObject.get("chg_date")==null?"":DateUtil.formatDate(new Date(Long.parseLong(jsonArrayJSONObject.get("chg_date").toString()))));
                    processor.transfer(i,  "biandonggushu","chg_shares_num");
                    processor.transfer(i,  "junjia","trans_avg_price");
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

    protected List<Long> getTimestampList() {
        List<Long> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            DateTime offset = DateUtil.offset(DateTime.now(), DateField.MONTH, -i);
            String format = DateUtil.format(offset, "MM");
            int month = Integer.parseInt(format);
            if (month==12||month==3||month==6||month==9){
                DateTime dateTime = DateUtil.endOfMonth(offset);
                Long time = DateUtil.parseDateTime(DateUtil.format(dateTime, "yyyy-MM-dd") + " 00:00:00").getTime();
                list.add( time);
            }
        }
        return list;
    }




}
