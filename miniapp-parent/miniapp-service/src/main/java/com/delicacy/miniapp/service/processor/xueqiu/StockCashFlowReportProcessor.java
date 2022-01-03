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
//                    processor.transfer(i, "jingyinghuodongchanshengdexianjinliuliang", "");
                    processor.transfer(i, "xiaoshoushangpin_tigonglaowushoudaodexianjin", "net_cash_of_disposal_assets");
                    processor.transfer(i, "shoudaodeshuifeifanhuan", "refund_of_tax_and_levies");
                    processor.transfer(i, "shoudaoqitayujingyinghuodongyouguandexianjin", "cash_received_of_othr_oa");
                    processor.transfer(i, "jingyinghuodongxianjinliuruxiaoji", "sub_total_of_ci_from_oa");
                    processor.transfer(i, "goumaishangpin_jieshoulaowuzhifudexianjin", "goods_buy_and_service_cash_pay");
                    processor.transfer(i, "zhifugeizhigongyijiweizhigongzhifudexianjin", "cash_paid_to_employee_etc");
                    processor.transfer(i, "zhifudegexiangshuifei", "payments_of_all_taxes");
                    processor.transfer(i, "zhifuqitayujingyinghuodongyouguandexianjin", "othrcash_paid_relating_to_oa");
                    processor.transfer(i, "jingyinghuodongxianjinliuchuxiaoji", "sub_total_of_cos_from_oa");
                    processor.transfer(i, "jingyinghuodongchanshengdexianjinliuliangjinge", "ncf_from_oa");
//                    processor.transfer(i, "touzihuodongchanshengdexianjinliuliang", "");
                    processor.transfer(i, "shouhuitouzishoudaodexianjin", "cash_received_of_dspsl_invest");
                    processor.transfer(i, "qudetouzishouyishoudaodexianjin", "sub_total_of_ci_from_ia");
                    processor.transfer(i, "chuzhigudingzichan_wuxingzichanheqitachangqizichanshouhuidexianjinjinge", "invest_income_cash_received");
                    processor.transfer(i, "chuzhizigongsijijitayingyedanweishoudaodexianjinjinge", "net_cash_of_disposal_branch");
                    processor.transfer(i, "shoudaoqitayutouzihuodongyouguandexianjin", "cash_received_of_othr_ia");
                    processor.transfer(i, "touzihuodongxianjinliuruxiaoji", "sub_total_of_ci_from_ia");
                    processor.transfer(i, "goujiangudingzichan_wuxingzichanheqitachangqizichanzhifudexianjin", "cash_paid_for_assets");
                    processor.transfer(i, "touzizhifudexianjin", "invest_paid_cash");
                    processor.transfer(i, "qudezigongsijijitayingyedanweizhifudexianjinjinge", "net_cash_amt_from_branch");
                    processor.transfer(i, "zhifuqitayutouzihuodongyouguandexianjin", "othrcash_paid_relating_to_ia");
                    processor.transfer(i, "touzihuodongxianjinliuchuxiaoji", "sub_total_of_cos_from_ia");
                    processor.transfer(i, "touzihuodongchanshengdexianjinliuliangjinge", "ncf_from_ia");
//                    processor.transfer(i, "chouzihuodongchanshengdexianjinliuliang", "");
                    processor.transfer(i, "xishoutouzishoudaodexianjin", "cash_received_of_absorb_invest");
                    processor.transfer(i, "qizhong_zigongsixishoushaoshugudongtouzishoudaodexianjin", "cash_received_from_investor");
                    processor.transfer(i, "qudejiekuanshoudaodexianjin", "cash_received_of_borrowing");
                    processor.transfer(i, "fahangzhaiquanshoudaodexianjin", "cash_received_from_bond_issue");
                    processor.transfer(i, "shoudaoqitayuchouzihuodongyouguandexianjin", "cash_received_of_othr_fa");
                    processor.transfer(i, "chouzihuodongxianjinliuruxiaoji", "sub_total_of_ci_from_fa");
                    processor.transfer(i, "changhuanzhaiwuzhifudexianjin", "cash_pay_for_debt");
                    processor.transfer(i, "fenpeiguli_lirunhuochangfulixizhifudexianjin", "cash_received_from_investor");
                    processor.transfer(i, "qizhong_zigongsizhifugeishaoshugudongdeguli", "branch_paid_to_minority_holder");
                    processor.transfer(i, "zhifuqitayuchouzihuodongyouguandexianjin", "othrcash_paid_relating_to_fa");
                    processor.transfer(i, "chouzihuodongxianjinliuchuxiaoji", "sub_total_of_cos_from_fa");
                    processor.transfer(i, "chouzihuodongchanshengdexianjinliuliangjinge", "ncf_from_fa");
                    processor.transfer(i, "huilvbiandongduixianjinjixianjindengjiawudeyingxiang", "effect_of_exchange_chg_on_cce");
                    processor.transfer(i, "xianjinjixianjindengjiawujingzengjiae", "net_increase_in_cce");
                    processor.transfer(i, "jia_qichuxianjinjixianjindengjiawuyue", "initial_balance_of_cce");
                    processor.transfer(i, "qimoxianjinjixianjindengjiawuyue", "final_balance_of_cce");
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
