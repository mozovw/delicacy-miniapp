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
//                    transfer(map, jsonArrayJSONObject, "liudongzichan", "");
                    transfer(map, jsonArrayJSONObject, "huobizijin", "currency_funds");
                    transfer(map, jsonArrayJSONObject, "jiaoyixingjinrongzichan", "tradable_fnncl_assets");
                    transfer(map, jsonArrayJSONObject, "yingshoupiaojujiyingshouzhangkuan", "ar_and_br");
                    transfer(map, jsonArrayJSONObject, "qizhong_yingshoupiaoju", "bills_receivable");
                    transfer(map, jsonArrayJSONObject, "yingshouzhangkuan", "account_receivable");
                    transfer(map, jsonArrayJSONObject, "yufukuanxiang", "pre_payment");
                    transfer(map, jsonArrayJSONObject, "yingshoulixi", "interest_receivable");
                    transfer(map, jsonArrayJSONObject, "yingshouguli", "dividend_receivable");
                    transfer(map, jsonArrayJSONObject, "qitayingshoukuan", "bill_payable");
                    transfer(map, jsonArrayJSONObject, "cunhuo", "inventory");
                    transfer(map, jsonArrayJSONObject, "hetongzichan", "contractual_assets");
                    transfer(map, jsonArrayJSONObject, "huafenweichiyoudaishoudezichan", "");
                    transfer(map, jsonArrayJSONObject, "yiniannadaoqidefeiliudongzichan", "");
                    transfer(map, jsonArrayJSONObject, "qitaliudongzichan", "earned_surplus");
                    transfer(map, jsonArrayJSONObject, "liudongzichanheji", "total_current_assets");
//                    transfer(map, jsonArrayJSONObject, "feiliudongzichan", "");
                    transfer(map, jsonArrayJSONObject, "kegongchushoujinrongzichan", "");
                    transfer(map, jsonArrayJSONObject, "chiyouzhidaoqitouzi", "");
                    transfer(map, jsonArrayJSONObject, "changqiyingshoukuan", "lt_receivable");
                    transfer(map, jsonArrayJSONObject, "changqiguquantouzi", "lt_equity_invest");
                    transfer(map, jsonArrayJSONObject, "qitaquanyigongjutouzi", "other_eq_ins_invest");
                    transfer(map, jsonArrayJSONObject, "qitafeiliudongjinrongzichan", "other_illiquid_fnncl_assets");
                    transfer(map, jsonArrayJSONObject, "touzixingfangdichan", "");
                    transfer(map, jsonArrayJSONObject, "gudingzichanheji", "fixed_asset_sum");
                    transfer(map, jsonArrayJSONObject, "qizhong_gudingzichan", "fixed_asset");
                    transfer(map, jsonArrayJSONObject, "gudingzichanqingli", "");
                    transfer(map, jsonArrayJSONObject, "zaijianɡonɡchenɡheji", "construction_in_process_sum");
                    transfer(map, jsonArrayJSONObject, "qizhong_zaijianɡonɡchenɡ", "construction_in_process");
                    transfer(map, jsonArrayJSONObject, "ɡonɡchenɡwuzi", "project_goods_and_material");
                    transfer(map, jsonArrayJSONObject, "shengchanxingshenɡwuzichan", "");
                    transfer(map, jsonArrayJSONObject, "youqizichan", "");
                    transfer(map, jsonArrayJSONObject, "wuxingzichan", "intangible_assets");
                    transfer(map, jsonArrayJSONObject, "kaifazhichu", "");
                    transfer(map, jsonArrayJSONObject, "shangyu", "goodwill");
                    transfer(map, jsonArrayJSONObject, "changqidaitanfeiyong", "lt_deferred_expense");
                    transfer(map, jsonArrayJSONObject, "diyansuodeshuizichan", "dt_assets");
                    transfer(map, jsonArrayJSONObject, "qitafeiliudongzichan", "othr_noncurrent_assets");
                    transfer(map, jsonArrayJSONObject, "feiliudongzichanheji", "total_noncurrent_assets");
                    transfer(map, jsonArrayJSONObject, "zichanheji", "total_assets");
//                    transfer(map, jsonArrayJSONObject, "liudongfuzhai", "");
                    transfer(map, jsonArrayJSONObject, "duanqijiekuan", "st_loan");
                    transfer(map, jsonArrayJSONObject, "jiaoyixingjinrongfuzhai", "tradable_fnncl_liab");
                    transfer(map, jsonArrayJSONObject, "yanshenɡjinrongfuzhai", "");
                    transfer(map, jsonArrayJSONObject, "yingfupiaojujiyingfuzhangkuan", "bp_and_ap");
                    transfer(map, jsonArrayJSONObject, "yingfupiaoju", "bill_payable");
                    transfer(map, jsonArrayJSONObject, "yingfuzhangkuan", "accounts_payable");
                    transfer(map, jsonArrayJSONObject, "yushoukuanxiang", "pre_receivable");
                    transfer(map, jsonArrayJSONObject, "hetongfuzhai", "contract_liabilities");
                    transfer(map, jsonArrayJSONObject, "yingfuzhigongxinchou", "payroll_payable");
                    transfer(map, jsonArrayJSONObject, "yingjiaoshuifei", "tax_payable");
                    transfer(map, jsonArrayJSONObject, "yingfulixi", "interest_payable");
                    transfer(map, jsonArrayJSONObject, "yingfuguli", "dividend_payable");
                    transfer(map, jsonArrayJSONObject, "qitayingfukuan", "othr_payables");
                    transfer(map, jsonArrayJSONObject, "huafenweichiyoudaishoudefuzhai", "");
                    transfer(map, jsonArrayJSONObject, "yiniannadaoqidefeiliudongfuzhai", "noncurrent_liab_due_in1y");
                    transfer(map, jsonArrayJSONObject, "qitaliudongfuzhai", "othr_current_liab");
                    transfer(map, jsonArrayJSONObject, "liudongfuzhaiheji", "total_current_liab");
//                    transfer(map, jsonArrayJSONObject, "feiliudongfuzhai", "");
                    transfer(map, jsonArrayJSONObject, "changqijiekuan", "lt_loan");
                    transfer(map, jsonArrayJSONObject, "yingfuzhaiquan", "bond_payable");
                    transfer(map, jsonArrayJSONObject, "changqiyingfukuanheji", "lt_payable_sum");
                    transfer(map, jsonArrayJSONObject, "changqiyingfukuan", "lt_payable");
                    transfer(map, jsonArrayJSONObject, "zhuanxiangyingfukuan", "special_payable");
                    transfer(map, jsonArrayJSONObject, "yujifuzhai", "estimated_liab");
                    transfer(map, jsonArrayJSONObject, "diyansuodeshuifuzhai", "dt_liab");
                    transfer(map, jsonArrayJSONObject, "diyanshouyi-feiliudongfuzhai", "noncurrent_liab_di");
                    transfer(map, jsonArrayJSONObject, "qitafeiliudongfuzhai", "othr_non_current_liab");
                    transfer(map, jsonArrayJSONObject, "feiliudongfuzhaiheji", "total_noncurrent_liab");
                    transfer(map, jsonArrayJSONObject, "fuzhaiheji", "total_liab");
//                    transfer(map, jsonArrayJSONObject, "suoyouzhequanyi", "");
                    transfer(map, jsonArrayJSONObject, "shishouziben(huoguben)", "shares");
                    transfer(map, jsonArrayJSONObject, "qitaquanyigongju", "othr_equity_instruments");
                    transfer(map, jsonArrayJSONObject, "qizhong_youxiangu", "");
                    transfer(map, jsonArrayJSONObject, "yongxuzhai", "perpetual_bond");
                    transfer(map, jsonArrayJSONObject, "zibengongji", "capital_reserve");
                    transfer(map, jsonArrayJSONObject, "jian_kucungu", "treasury_stock");
                    transfer(map, jsonArrayJSONObject, "qitazongheshouyi", "othr_compre_income");
                    transfer(map, jsonArrayJSONObject, "zhuanxiangchubei", "special_reserve");
                    transfer(map, jsonArrayJSONObject, "yingyugongji", "earned_surplus");
                    transfer(map, jsonArrayJSONObject, "weifenpeilirun", "undstrbtd_profit");
                    transfer(map, jsonArrayJSONObject, "yibanfengxianzhunbei", "general_risk_provision");
                    transfer(map, jsonArrayJSONObject, "waibibaobiaozhesuanchae", "");
                    transfer(map, jsonArrayJSONObject, "guishuyumugongsigudongquanyiheji", "total_quity_atsopc");
                    transfer(map, jsonArrayJSONObject, "shaoshugudongquanyi", "minority_equity");
                    transfer(map, jsonArrayJSONObject, "gudongquanyiheji", "total_holders_equity");
                    transfer(map, jsonArrayJSONObject, "fuzhaihegudongquanyizongji", "total_liab_and_holders_equity");

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
