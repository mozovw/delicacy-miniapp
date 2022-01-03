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
public class StockProfitReportProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/finance/";


    static final String URL_PRE_HK[] = {
            "https://stock.xueqiu.com/v5/stock/finance/hk/income.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/income.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/income.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/income.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
    };

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/finance/cn/income.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/income.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/income.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/income.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
    };

    volatile boolean flag = false;

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
                    processor.transfer(i, "yingyezongshouru", "total_revenue");
                    processor.transfer(i, "qizhong_yingyeshouru", "revenue");

                    processor.transfer(i, "touzishouyi", "invest_income");
                    processor.transfer(i, "qizhong_duilianyingqiyeheyingqiyedetouzishouyi", "invest_incomes_from_rr");
                    processor.transfer(i, "gongyunjiazhibiandongshouyi", "income_from_chg_in_fv");
                    processor.transfer(i, "qitashouyi", "other_income");

                    processor.transfer(i, "yingyezongchengben", "operating_payout");
                    processor.transfer(i, "qizhong_yingyechengben", "operating_cost");

                    processor.transfer(i, "yingyeshuijinjifujia", "operating_taxes_and_surcharge");
                    processor.transfer(i, "xiaoshoufeiyong", "sales_fee");
                    processor.transfer(i, "guanlifeiyong", "manage_fee");
                    processor.transfer(i, "yanfafeiyong", "rad_cost");
                    processor.transfer(i, "caiwufeiyong", "financing_expenses");
                    processor.transfer(i, "qizhong_lixifeiyong", "finance_cost_interest_fee");
                    processor.transfer(i, "lixishouru", "finance_cost_interest_income");
                    processor.transfer(i, "zichanchuzhishouyi", "asset_disposal_income");


                    processor.transfer(i, "zichanjianzhisunshi", "asset_impairment_loss");
                    processor.transfer(i, "xinyongjianzhisunshi", "credit_impairment_loss");
                    processor.transfer(i, "yingyelirun", "op");
                    processor.transfer(i, "jia_yingyewaishouru", "non_operating_income");
                    processor.transfer(i, "jian_yingyewaichichu", "non_operating_payout");
                    processor.transfer(i, "lirunzonge", "profit_total_amt");
                    processor.transfer(i, "jian_suodeshuifeiyong", "income_tax_expenses");
                    processor.transfer(i, "jinglirun", "net_profit");
                    processor.transfer(i, "guishuyumugongsigudongdejinglirun", "net_profit_atsopc");
                    processor.transfer(i, "shaoshugudongquanyi", "minority_gal");
                    processor.transfer(i, "kouchufeijingchangxingsuiyihoudejinglirun", "net_profit_after_nrgal_atsolc");

                    processor.transfer(i, "jibeimeigushouyi", "basic_eps");
                    processor.transfer(i, "xishimeigushouyi", "dlt_earnings_per_share");
                    processor.transfer(i, "qitazongheshouyi", "othr_compre_income");
                    processor.transfer(i, "guishumugongsisuoyouzhedeqitazonghequanyi", "othr_compre_income_atoopc");
                    processor.transfer(i, "guishuyushaoshugudongdeqitazongheshouyi", "othr_compre_income_atms");
                    processor.transfer(i, "zongheshouyizonge", "total_compre_income");
                    processor.transfer(i, "guishuyumugongsigudongdezongheshouyizonge", "total_compre_income_atsopc");
                    processor.transfer(i, "guishuyushaoshugudongdezongheshouyizonge", "total_compre_income_atms");
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
