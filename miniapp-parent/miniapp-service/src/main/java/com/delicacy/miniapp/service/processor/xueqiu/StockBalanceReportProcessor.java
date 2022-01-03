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
public class StockBalanceReportProcessor extends AbstactProcessor {


    static final String URL_POST = "https://stock.xueqiu.com/v5/stock/finance/";


    static final String URL_PRE_HK[] = {
            "https://stock.xueqiu.com/v5/stock/finance/hk/balance.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/balance.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/balance.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/hk/balance.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
    };

    static final String URL_PRE[] = {
            "https://stock.xueqiu.com/v5/stock/finance/cn/balance.json?symbol=%s&type=Q1&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/balance.json?symbol=%s&type=Q2&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/balance.json?symbol=%s&type=Q3&is_detail=true&count=5&timestamp=%s",
            "https://stock.xueqiu.com/v5/stock/finance/cn/balance.json?symbol=%s&type=Q4&is_detail=true&count=5&timestamp=%s"
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
//                    processor.transfer(i, "liudongzichan", "");
                    processor.transfer(i, "huobizijin", "currency_funds");
                    processor.transfer(i, "jiaoyixingjinrongzichan", "tradable_fnncl_assets");
                    processor.transfer(i, "yingshoupiaojujiyingshouzhangkuan", "ar_and_br");
                    processor.transfer(i, "qizhong_yingshoupiaoju", "bills_receivable");
                    processor.transfer(i, "yingshouzhangkuan", "account_receivable");
                    processor.transfer(i, "yufukuanxiang", "pre_payment");
                    processor.transfer(i, "yingshoulixi", "interest_receivable");
                    processor.transfer(i, "yingshouguli", "dividend_receivable");
                    processor.transfer(i, "qitayingshoukuan", "bill_payable");
                    processor.transfer(i, "cunhuo", "inventory");
                    processor.transfer(i, "hetongzichan", "contractual_assets");
                    processor.transfer(i, "huafenweichiyoudaishoudezichan", "");
                    processor.transfer(i, "yiniannadaoqidefeiliudongzichan", "");
                    processor.transfer(i, "qitaliudongzichan", "earned_surplus");
                    processor.transfer(i, "liudongzichanheji", "total_current_assets");
//                    processor.transfer(i, "feiliudongzichan", "");
                    processor.transfer(i, "kegongchushoujinrongzichan", "");
                    processor.transfer(i, "chiyouzhidaoqitouzi", "");
                    processor.transfer(i, "changqiyingshoukuan", "lt_receivable");
                    processor.transfer(i, "changqiguquantouzi", "lt_equity_invest");
                    processor.transfer(i, "qitaquanyigongjutouzi", "other_eq_ins_invest");
                    processor.transfer(i, "qitafeiliudongjinrongzichan", "other_illiquid_fnncl_assets");
                    processor.transfer(i, "touzixingfangdichan", "");
                    processor.transfer(i, "gudingzichanheji", "fixed_asset_sum");
                    processor.transfer(i, "qizhong_gudingzichan", "fixed_asset");
                    processor.transfer(i, "gudingzichanqingli", "");
                    processor.transfer(i, "zaijianɡonɡchenɡheji", "construction_in_process_sum");
                    processor.transfer(i, "qizhong_zaijianɡonɡchenɡ", "construction_in_process");
                    processor.transfer(i, "ɡonɡchenɡwuzi", "project_goods_and_material");
                    processor.transfer(i, "shengchanxingshenɡwuzichan", "");
                    processor.transfer(i, "youqizichan", "");
                    processor.transfer(i, "wuxingzichan", "intangible_assets");
                    processor.transfer(i, "kaifazhichu", "");
                    processor.transfer(i, "shangyu", "goodwill");
                    processor.transfer(i, "changqidaitanfeiyong", "lt_deferred_expense");
                    processor.transfer(i, "diyansuodeshuizichan", "dt_assets");
                    processor.transfer(i, "qitafeiliudongzichan", "othr_noncurrent_assets");
                    processor.transfer(i, "feiliudongzichanheji", "total_noncurrent_assets");
                    processor.transfer(i, "zichanheji", "total_assets");
//                    processor.transfer(i, "liudongfuzhai", "");
                    processor.transfer(i, "duanqijiekuan", "st_loan");
                    processor.transfer(i, "jiaoyixingjinrongfuzhai", "tradable_fnncl_liab");
                    processor.transfer(i, "yanshenɡjinrongfuzhai", "");
                    processor.transfer(i, "yingfupiaojujiyingfuzhangkuan", "bp_and_ap");
                    processor.transfer(i, "yingfupiaoju", "bill_payable");
                    processor.transfer(i, "yingfuzhangkuan", "accounts_payable");
                    processor.transfer(i, "yushoukuanxiang", "pre_receivable");
                    processor.transfer(i, "hetongfuzhai", "contract_liabilities");
                    processor.transfer(i, "yingfuzhigongxinchou", "payroll_payable");
                    processor.transfer(i, "yingjiaoshuifei", "tax_payable");
                    processor.transfer(i, "yingfulixi", "interest_payable");
                    processor.transfer(i, "yingfuguli", "dividend_payable");
                    processor.transfer(i, "qitayingfukuan", "othr_payables");
                    processor.transfer(i, "huafenweichiyoudaishoudefuzhai", "");
                    processor.transfer(i, "yiniannadaoqidefeiliudongfuzhai", "noncurrent_liab_due_in1y");
                    processor.transfer(i, "qitaliudongfuzhai", "othr_current_liab");
                    processor.transfer(i, "liudongfuzhaiheji", "total_current_liab");
//                    processor.transfer(i, "feiliudongfuzhai", "");
                    processor.transfer(i, "changqijiekuan", "lt_loan");
                    processor.transfer(i, "yingfuzhaiquan", "bond_payable");
                    processor.transfer(i, "changqiyingfukuanheji", "lt_payable_sum");
                    processor.transfer(i, "changqiyingfukuan", "lt_payable");
                    processor.transfer(i, "zhuanxiangyingfukuan", "special_payable");
                    processor.transfer(i, "yujifuzhai", "estimated_liab");
                    processor.transfer(i, "diyansuodeshuifuzhai", "dt_liab");
                    processor.transfer(i, "diyanshouyi-feiliudongfuzhai", "noncurrent_liab_di");
                    processor.transfer(i, "qitafeiliudongfuzhai", "othr_non_current_liab");
                    processor.transfer(i, "feiliudongfuzhaiheji", "total_noncurrent_liab");
                    processor.transfer(i, "fuzhaiheji", "total_liab");
//                    processor.transfer(i, "suoyouzhequanyi", "");
                    processor.transfer(i, "shishouziben(huoguben)", "shares");
                    processor.transfer(i, "qitaquanyigongju", "othr_equity_instruments");
                    processor.transfer(i, "qizhong_youxiangu", "");
                    processor.transfer(i, "yongxuzhai", "perpetual_bond");
                    processor.transfer(i, "zibengongji", "capital_reserve");
                    processor.transfer(i, "jian_kucungu", "treasury_stock");
                    processor.transfer(i, "qitazongheshouyi", "othr_compre_income");
                    processor.transfer(i, "zhuanxiangchubei", "special_reserve");
                    processor.transfer(i, "yingyugongji", "earned_surplus");
                    processor.transfer(i, "weifenpeilirun", "undstrbtd_profit");
                    processor.transfer(i, "yibanfengxianzhunbei", "general_risk_provision");
                    processor.transfer(i, "waibibaobiaozhesuanchae", "");
                    processor.transfer(i, "guishuyumugongsigudongquanyiheji", "total_quity_atsopc");
                    processor.transfer(i, "shaoshugudongquanyi", "minority_equity");
                    processor.transfer(i, "gudongquanyiheji", "total_holders_equity");
                    processor.transfer(i, "fuzhaihegudongquanyizongji", "total_liab_and_holders_equity");

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
