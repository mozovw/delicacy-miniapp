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
public class StockTopHoldersProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/f10/";


    static final String URL_PRE_HK[] = {

    };

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/f10/cn/top_holders.json?symbol=%s&locate=%s",
    };

    volatile boolean flag = false;

    public void setAppointReportDates(String[] appointReportDates) {
        this.appointReportDates = appointReportDates;
    }

    private String appointReportDates[] = {};

    private void transfer(Map page, Object jsonObject, String a, String b) {
        if (b == null) {
            page.put(a, null);
            return;
        }
        Object obj = null;

        if (jsonObject instanceof JSONObject) {
            obj = ((JSONObject) jsonObject).get(b);
        } else {
            if (jsonObject != null) {
                obj = jsonObject;
            }
        }

        if (obj == null) {
            page.put(a, null);
            return;
        }

        String string = null;
        if (obj instanceof String) {
            string = String.valueOf(obj);
        } else if (obj instanceof BigDecimal) {
            string = ((BigDecimal) obj).setScale(3, RoundingMode.HALF_UP).toString();
        } else if (obj instanceof Long) {
            string = ((Long) obj).toString();
        } else if (obj instanceof Integer) {
            string = ((Integer) obj).toString();
        } else if (obj instanceof JSONArray) {
            transfer(page, ((JSONArray) obj).get(0), a, b);
            return;
        }
        page.put(a, string);
    }

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

            LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonArrayJSONObject = jsonArray.getJSONObject(i);
                LinkedHashMap map = new LinkedHashMap();
                map.put("symbol", symbol.replace("SH", "").replace("SZ", ""));

                if (url.contains("cn")) {
                    transfer(map, jsonArrayJSONObject, "report_date", "report_name");
                    Object report_date = map.get("report_date");
                    if (appointReportDates.length != 0 && Arrays.stream(appointReportDates).noneMatch(e -> e.equalsIgnoreCase(String.valueOf(report_date)))) {
                        continue;
                    }
                    transfer(map, jsonArrayJSONObject, "huobizijin", "currency_funds");

                }
                // todo 没做处理
                if (url.contains("hk")) {
                    transfer(map, jsonArrayJSONObject, "report_date", "report_name");
                    Object report_date = map.get("report_date");
                    if (appointReportDates.length != 0 && Arrays.stream(appointReportDates).noneMatch(e -> e.equalsIgnoreCase(String.valueOf(report_date)))) {
                        continue;
                    }
                }


                mapMain.put(i, map);
            }

            page.putField("map", mapMain);


        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            if (flag) {
                return;
            }
            Map<String, List<String>> listMap = HttpUtil.decodeParams(url, "utf-8");
            String market = listMap.get("market").get(0);


            String rawText = page.getRawText();
            JSONObject jsonObject = JSON.parseObject(rawText);
            rawText = jsonObject.get("data").toString();
            jsonObject = JSON.parseObject(rawText);
            rawText = jsonObject.get("list").toString();
            JSONArray jsonArray = JSON.parseArray(rawText);

            List<String> collect = new ArrayList<>();

            String[] urls = new String[0];
            if ("CN".equals(market)) {
                urls = URL_PRE;
            }
            if ("HK".equals(market)) {
                urls = URL_PRE_HK;
            }

            String[] finalUrls = urls;
            jsonArray.stream().forEach(e -> {
                JSONObject e1 = (JSONObject) e;
                String symbol = e1.get("symbol").toString();

                Arrays.stream(finalUrls).forEach(ee -> {
                    getTimestampList().forEach(eee->{
                        collect.add(String.format(ee, symbol, eee));
                    });
                });
            });
            page.addTargetRequests(collect);
            // update flag
            Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
            long longPage = Long.parseLong(stringListMap.get("page").get(0));
            long sum = longPage * Long.parseLong(stringListMap.get("size").get(0));
            long count = Long.parseLong(jsonObject.get("count").toString());
            flag = count < sum;
            // update page
            // get newurl
            stringListMap.put("page", Lists.newArrayList(String.valueOf(longPage + 1)));
            String params = HttpUtil.toParams(stringListMap);
            String string = page.getUrl().toString();
            String newUrl = string.substring(0, string.indexOf("?") + 1) + params;
            page.addTargetRequest(newUrl);
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
