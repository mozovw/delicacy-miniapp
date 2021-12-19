package com.delicacy.miniapp.service.processor.xueqiu;

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
public class StockCashFlowReportProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/finance/";


    static final String URL_PRE_HK[] = {
            "https://stock.xueqiu.com/v5/stock/finance/hk/cash_flow.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/cash_flow.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/cash_flow.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/cash_flow.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
    };

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/finance/cn/cash_flow.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/cash_flow.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/cash_flow.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/cash_flow.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
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
            JSONArray jsonArray = jsonObject.getJSONArray("list");

            LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonArrayJSONObject = jsonArray.getJSONObject(i);
                LinkedHashMap map = new LinkedHashMap();
                map.put("symbol", symbol.replace("SH", "").replace("SZ", ""));
                transfer(map, jsonObject, "name", "quote_name");

                if (url.contains("cn")) {
                    transfer(map, jsonArrayJSONObject, "report_date", "report_name");
                    Object report_date = map.get("report_date");
                    if (appointReportDates.length != 0 && Arrays.stream(appointReportDates).noneMatch(e -> e.equalsIgnoreCase(String.valueOf(report_date)))) {
                        continue;
                    }
//                    transfer(map, jsonArrayJSONObject, "jingyinghuodongchanshengdexianjinliuliang", "");
                    transfer(map, jsonArrayJSONObject, "xiaoshoushangpin_tigonglaowushoudaodexianjin", "net_cash_of_disposal_assets");
                    transfer(map, jsonArrayJSONObject, "shoudaodeshuifeifanhuan", "refund_of_tax_and_levies");
                    transfer(map, jsonArrayJSONObject, "shoudaoqitayujingyinghuodongyouguandexianjin", "cash_received_of_othr_oa");
                    transfer(map, jsonArrayJSONObject, "jingyinghuodongxianjinliuruxiaoji", "sub_total_of_ci_from_oa");
                    transfer(map, jsonArrayJSONObject, "goumaishangpin_jieshoulaowuzhifudexianjin", "goods_buy_and_service_cash_pay");
                    transfer(map, jsonArrayJSONObject, "zhifugeizhigongyijiweizhigongzhifudexianjin", "cash_paid_to_employee_etc");
                    transfer(map, jsonArrayJSONObject, "zhifudegexiangshuifei", "payments_of_all_taxes");
                    transfer(map, jsonArrayJSONObject, "zhifuqitayujingyinghuodongyouguandexianjin", "othrcash_paid_relating_to_oa");
                    transfer(map, jsonArrayJSONObject, "jingyinghuodongxianjinliuchuxiaoji", "sub_total_of_cos_from_oa");
                    transfer(map, jsonArrayJSONObject, "jingyinghuodongchanshengdexianjinliuliangjinge", "ncf_from_oa");
//                    transfer(map, jsonArrayJSONObject, "touzihuodongchanshengdexianjinliuliang", "");
                    transfer(map, jsonArrayJSONObject, "shouhuitouzishoudaodexianjin", "cash_received_of_dspsl_invest");
                    transfer(map, jsonArrayJSONObject, "qudetouzishouyishoudaodexianjin", "sub_total_of_ci_from_ia");
                    transfer(map, jsonArrayJSONObject, "chuzhigudingzichan_wuxingzichanheqitachangqizichanshouhuidexianjinjinge", "invest_income_cash_received");
                    transfer(map, jsonArrayJSONObject, "chuzhizigongsijijitayingyedanweishoudaodexianjinjinge", "net_cash_of_disposal_branch");
                    transfer(map, jsonArrayJSONObject, "shoudaoqitayutouzihuodongyouguandexianjin", "cash_received_of_othr_ia");
                    transfer(map, jsonArrayJSONObject, "touzihuodongxianjinliuruxiaoji", "sub_total_of_ci_from_ia");
                    transfer(map, jsonArrayJSONObject, "goujiangudingzichan_wuxingzichanheqitachangqizichanzhifudexianjin", "cash_paid_for_assets");
                    transfer(map, jsonArrayJSONObject, "touzizhifudexianjin", "invest_paid_cash");
                    transfer(map, jsonArrayJSONObject, "qudezigongsijijitayingyedanweizhifudexianjinjinge", "net_cash_amt_from_branch");
                    transfer(map, jsonArrayJSONObject, "zhifuqitayutouzihuodongyouguandexianjin", "othrcash_paid_relating_to_ia");
                    transfer(map, jsonArrayJSONObject, "touzihuodongxianjinliuchuxiaoji", "sub_total_of_cos_from_ia");
                    transfer(map, jsonArrayJSONObject, "touzihuodongchanshengdexianjinliuliangjinge", "ncf_from_ia");
//                    transfer(map, jsonArrayJSONObject, "chouzihuodongchanshengdexianjinliuliang", "");
                    transfer(map, jsonArrayJSONObject, "xishoutouzishoudaodexianjin", "cash_received_of_absorb_invest");
                    transfer(map, jsonArrayJSONObject, "qizhong_zigongsixishoushaoshugudongtouzishoudaodexianjin", "cash_received_from_investor");
                    transfer(map, jsonArrayJSONObject, "qudejiekuanshoudaodexianjin", "cash_received_of_borrowing");
                    transfer(map, jsonArrayJSONObject, "fahangzhaiquanshoudaodexianjin", "cash_received_from_bond_issue");
                    transfer(map, jsonArrayJSONObject, "shoudaoqitayuchouzihuodongyouguandexianjin", "cash_received_of_othr_fa");
                    transfer(map, jsonArrayJSONObject, "chouzihuodongxianjinliuruxiaoji", "sub_total_of_ci_from_fa");
                    transfer(map, jsonArrayJSONObject, "changhuanzhaiwuzhifudexianjin", "cash_pay_for_debt");
                    transfer(map, jsonArrayJSONObject, "fenpeiguli_lirunhuochangfulixizhifudexianjin", "cash_received_from_investor");
                    transfer(map, jsonArrayJSONObject, "qizhong_zigongsizhifugeishaoshugudongdeguli", "branch_paid_to_minority_holder");
                    transfer(map, jsonArrayJSONObject, "zhifuqitayuchouzihuodongyouguandexianjin", "othrcash_paid_relating_to_fa");
                    transfer(map, jsonArrayJSONObject, "chouzihuodongxianjinliuchuxiaoji", "sub_total_of_cos_from_fa");
                    transfer(map, jsonArrayJSONObject, "chouzihuodongchanshengdexianjinliuliangjinge", "ncf_from_fa");
                    transfer(map, jsonArrayJSONObject, "huilvbiandongduixianjinjixianjindengjiawudeyingxiang", "effect_of_exchange_chg_on_cce");
                    transfer(map, jsonArrayJSONObject, "xianjinjixianjindengjiawujingzengjiae", "net_increase_in_cce");
                    transfer(map, jsonArrayJSONObject, "jia_qichuxianjinjixianjindengjiawuyue", "initial_balance_of_cce");
                    transfer(map, jsonArrayJSONObject, "qimoxianjinjixianjindengjiawuyue", "final_balance_of_cce");

                }
                // todo 没做处理
                if (url.contains("hk")) {
                    transfer(map, jsonArrayJSONObject, "report_date", "report_name");
                    Object report_date = map.get("report_date");
                    if (appointReportDates.length != 0 && Arrays.stream(appointReportDates).noneMatch(e -> e.equalsIgnoreCase(String.valueOf(report_date)))) {
                        continue;
                    }
                    transfer(map, jsonArrayJSONObject, "yingyeshouru", "tto");
                    transfer(map, jsonArrayJSONObject, "jinglirun", "ploashh");
                    transfer(map, jsonArrayJSONObject, "meigushouyi", "beps");
                    transfer(map, jsonArrayJSONObject, "meigushouyitiaozhenghou", "beps_aju");
                    transfer(map, jsonArrayJSONObject, "meigujingzichan", "bps");
                    transfer(map, jsonArrayJSONObject, "meiguxianjinliujinge", "ncfps");
                    transfer(map, jsonArrayJSONObject, "meigujingyingxianjinliu", "nocfps");
                    transfer(map, jsonArrayJSONObject, "meigutouzixianjinliu", "ninvcfps");
                    transfer(map, jsonArrayJSONObject, "meiguchouzixianjinliu", "nfcgcfps");
                    transfer(map, jsonArrayJSONObject, "meiguyingyee", "ttops");
                    transfer(map, jsonArrayJSONObject, "meiguyingyeshouru", "tsrps");
                    transfer(map, jsonArrayJSONObject, "meiguyingyelirun", "opps");

                    transfer(map, jsonArrayJSONObject, "jingzichanshouyilv", "roe");
                    transfer(map, jsonArrayJSONObject, "zongzichanhuibaolv", "rota");
                    transfer(map, jsonArrayJSONObject, "maolilv", "gross_selling_rate");
                    transfer(map, jsonArrayJSONObject, "zichanfuzhailv", "tlia_ta");
                    transfer(map, jsonArrayJSONObject, "liudongbilv", "cro");
                    transfer(map, jsonArrayJSONObject, "sudongbilv", "qro");
                    transfer(map, jsonArrayJSONObject, "cunhuozhuanhuazhouqi", "ivcvspd");
                    transfer(map, jsonArrayJSONObject, "yingshouzhangkuanzhuanhuazhouqi", "arbcvspd");
                    transfer(map, jsonArrayJSONObject, "yingfuzhangkuanzhuanhuazhouqi", "apycvspd");
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
                    collect.add(String.format(ee, symbol, System.currentTimeMillis()));
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




}
