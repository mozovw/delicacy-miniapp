package com.delicacy.miniapp.service.processor.xueqiu;

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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
public class StockReportProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/finance/";

    static final String URL_PRE_HK[] = {
            "https://stock.xueqiu.com/v5/stock/finance/hk/indicator.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/indicator.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/indicator.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/indicator.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
    };

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/finance/cn/indicator.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/indicator.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/indicator.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/indicator.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
    };

    public void setAppointReportDates(String[] appointReportDates) {
        this.appointReportDates = appointReportDates;
    }

    private String appointReportDates[] = {};

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
            Object quote_name = jsonObject.get("quote_name");
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            PageProcessor processor = new PageProcessor(page,jsonArray);

            for (int i = 0; i < jsonArray.size(); i++) {
                processor.putmap(i,"symbol", symbol.replace("SH", "").replace("SZ", ""));
                processor.putmap(i,"name",String.valueOf(quote_name));
                if (url.contains("cn")) {
                    processor.transfer(i,"report_date", "report_name");
                    LinkedHashMap<String, String> getmap = processor.getmap(i);
                    Object report_date = getmap.get("report_date");
                    if (appointReportDates.length != 0 && Arrays.stream(appointReportDates).noneMatch(e -> e.equalsIgnoreCase(String.valueOf(report_date)))) {
                        processor.getmap(i).clear();
                        continue;
                    }
                    processor.transfer(i, "yingyeshouru", "total_revenue");
                    processor.transfer(i, "yingyeshourutongbizengzhang", "operating_income_yoy");
                    processor.transfer(i, "jinglirun", "net_profit_atsopc");
                    processor.transfer(i, "jingliruntongbizengzhang", "net_profit_atsopc_yoy");
                    processor.transfer(i, "koufeijinglirun", "net_profit_after_nrgal_atsolc");
                    processor.transfer(i, "koufeijingliruntongbizengzhang", "np_atsopc_nrgal_yoy");
                    processor.transfer(i, "meigushouyi", "basic_eps");
                    processor.transfer(i, "meigujingzichan", "np_per_share");
                    processor.transfer(i, "meiguzibengongjijin", "capital_reserve");
                    processor.transfer(i, "meiguweifenpeilirun", "undistri_profit_ps");
                    processor.transfer(i, "meigujingyingxianjinliu", "operate_cash_flow_ps");
                    processor.transfer(i, "jingzichanshouyilv", "avg_roe");
                    processor.transfer(i, "jingzichanshouyilv-tanbo", "ore_dlt");
                    processor.transfer(i, "zongzichanbaochoulv", "net_interest_of_total_assets");
                    processor.transfer(i, "renlitouruhuibaolv", "rop");
                    processor.transfer(i, "xiaoshoumaolilv", "gross_selling_rate");
                    processor.transfer(i, "xiaoshoujinglilv", "net_selling_rate");
                    processor.transfer(i, "zichanfuzhailv", "asset_liab_ratio");
                    processor.transfer(i, "liudongbilv", "current_ratio");
                    processor.transfer(i, "sudongbilv", "quick_ratio");
                    processor.transfer(i, "quanyichengshu", "equity_multiplier");
                    processor.transfer(i, "chanquanbilv", "equity_ratio");
                    processor.transfer(i, "gudongquanyibilv", "holder_equity");
                    processor.transfer(i, "xianjinliuliangbilv", "ncf_from_oa_to_total_liab");
                    processor.transfer(i, "cunhuozhouzhuantianshu", "inventory_turnover_days");
                    processor.transfer(i, "yingshouzhangkuanzhouzhuantianshu", "receivable_turnover_days");
                    processor.transfer(i, "yingfuzhangkuanzhouzhuantianshu", "accounts_payable_turnover_days");
                    processor.transfer(i, "xianjinxunhuanzhouqi", "cash_cycle");
                    processor.transfer(i, "zongzichanzhouzhuanlv", "total_capital_turnover");
                    processor.transfer(i, "cunhuozhouzhuanlv", "inventory_turnover");
                    processor.transfer(i, "yingshouzhangkuanzhouzhuanlv", "account_receivable_turnover");
                    processor.transfer(i, "yingfuzhangkuanzhouzhuanlv", "accounts_payable_turnover");
                    processor.transfer(i, "liudongzichanzhouzhuanlv", "current_asset_turnover_rate");
                    processor.transfer(i, "gudingzichanzhouzhuanlv", "fixed_asset_turnover_ratio");
                }

//                if (url.contains("hk")) {
//                    transfer(map, jsonArrayJSONObject, "report_date", "report_name");
//                    Object report_date = map.get("report_date");
//                    if (appointReportDates.length != 0 && Arrays.stream(appointReportDates).noneMatch(e -> e.equalsIgnoreCase(String.valueOf(report_date)))) {
//                        continue;
//                    }
//                    transfer(map, jsonArrayJSONObject, "yingyeshouru", "tto");
//                    transfer(map, jsonArrayJSONObject, "jinglirun", "ploashh");
//                    transfer(map, jsonArrayJSONObject, "meigushouyi", "beps");
//                    transfer(map, jsonArrayJSONObject, "meigushouyitiaozhenghou", "beps_aju");
//                    transfer(map, jsonArrayJSONObject, "meigujingzichan", "bps");
//                    transfer(map, jsonArrayJSONObject, "meiguxianjinliujinge", "ncfps");
//                    transfer(map, jsonArrayJSONObject, "meigujingyingxianjinliu", "nocfps");
//                    transfer(map, jsonArrayJSONObject, "meigutouzixianjinliu", "ninvcfps");
//                    transfer(map, jsonArrayJSONObject, "meiguchouzixianjinliu", "nfcgcfps");
//                    transfer(map, jsonArrayJSONObject, "meiguyingyee", "ttops");
//                    transfer(map, jsonArrayJSONObject, "meiguyingyeshouru", "tsrps");
//                    transfer(map, jsonArrayJSONObject, "meiguyingyelirun", "opps");
//
//                    transfer(map, jsonArrayJSONObject, "jingzichanshouyilv", "roe");
//                    transfer(map, jsonArrayJSONObject, "zongzichanhuibaolv", "rota");
//                    transfer(map, jsonArrayJSONObject, "maolilv", "gross_selling_rate");
//                    transfer(map, jsonArrayJSONObject, "zichanfuzhailv", "tlia_ta");
//                    transfer(map, jsonArrayJSONObject, "liudongbilv", "cro");
//                    transfer(map, jsonArrayJSONObject, "sudongbilv", "qro");
//                    transfer(map, jsonArrayJSONObject, "cunhuozhuanhuazhouqi", "ivcvspd");
//                    transfer(map, jsonArrayJSONObject, "yingshouzhangkuanzhuanhuazhouqi", "arbcvspd");
//                    transfer(map, jsonArrayJSONObject, "yingfuzhangkuanzhuanhuazhouqi", "apycvspd");
//                }
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
