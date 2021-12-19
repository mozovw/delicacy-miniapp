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
                    transfer(map, jsonArrayJSONObject, "yingyezongshouru", "total_revenue");
                    transfer(map, jsonArrayJSONObject, "qizhong_yingyeshouru", "revenue");
//                    transfer(map, jsonArrayJSONObject, "yizhuanbaofei", "earned_premium");
//                    transfer(map, jsonArrayJSONObject, "baoxianyewushouru", "insurance_income");
//                    transfer(map, jsonArrayJSONObject, "qizhong_fenbaofeishouru", "rein_premium_income");
//                    transfer(map, jsonArrayJSONObject, "jian_fenchubaofei", "ceded_out_premium");
//                    transfer(map, jsonArrayJSONObject, "tiquweidaoqizerenzhunbeijin", "draw_undueduty_deposit");
                    transfer(map, jsonArrayJSONObject, "touzishouyi", "invest_income");
                    transfer(map, jsonArrayJSONObject, "qizhong_duilianyingqiyeheyingqiyedetouzishouyi", "invest_incomes_from_rr");
                    transfer(map, jsonArrayJSONObject, "gongyunjiazhibiandongshouyi", "income_from_chg_in_fv");
//                    transfer(map, jsonArrayJSONObject, "huiduishouyi", "exchg_gain");
                    transfer(map, jsonArrayJSONObject, "qitashouyi", "other_income");
//                    transfer(map, jsonArrayJSONObject, "qitayewushouru", "othr_income");

                    transfer(map, jsonArrayJSONObject, "yingyezongchengben", "operating_payout");
                    transfer(map, jsonArrayJSONObject, "qizhong_yingyechengben", "operating_cost");

//                    transfer(map, jsonArrayJSONObject, "tuibaojin", "refunded_premium");
//                    transfer(map, jsonArrayJSONObject, "peifuzhichu", "compen_payout");
//                    transfer(map, jsonArrayJSONObject, "jian_tanhuipeifuzhichu", "compen_expense");
//                    transfer(map, jsonArrayJSONObject, "tiqubaoxianzerenzhunbeijin", "draw_duty_deposit");
//                    transfer(map, jsonArrayJSONObject, "jian_tanhuibaoxianzerenzhunbeijin", "amortized_deposit_for_duty");
//                    transfer(map, jsonArrayJSONObject, "baodanhonglizhichu", "commi_on_insurance_policy");
//                    transfer(map, jsonArrayJSONObject, "fenbaofeiyong", "rein_expenditure");

                    transfer(map, jsonArrayJSONObject, "yingyeshuijinjifujia", "operating_taxes_and_surcharge");
                    transfer(map, jsonArrayJSONObject, "xiaoshoufeiyong", "sales_fee");
                    transfer(map, jsonArrayJSONObject, "guanlifeiyong", "manage_fee");
                    transfer(map, jsonArrayJSONObject, "yanfafeiyong", "rad_cost");
                    transfer(map, jsonArrayJSONObject, "caiwufeiyong", "financing_expenses");
                    transfer(map, jsonArrayJSONObject, "qizhong_lixifeiyong", "finance_cost_interest_fee");
                    transfer(map, jsonArrayJSONObject, "lixishouru", "finance_cost_interest_income");
                    transfer(map, jsonArrayJSONObject, "zichanchuzhishouyi", "asset_disposal_income");

//                    transfer(map, jsonArrayJSONObject, "shouxufeijiyongjinzhichu", "charge_and_commi_expenses");
//                    transfer(map, jsonArrayJSONObject, "yewujiguanlifei", "business_and_manage_fee");
//                    transfer(map, jsonArrayJSONObject, "jian_tanhuifenbaofeiyong", "amortized_rein_expenditure");
//                    transfer(map, jsonArrayJSONObject, "qitayewuchengben", "othr_business_costs");

                    transfer(map, jsonArrayJSONObject, "zichanjianzhisunshi", "asset_impairment_loss");
                    transfer(map, jsonArrayJSONObject, "xinyongjianzhisunshi", "credit_impairment_loss");
                    transfer(map, jsonArrayJSONObject, "yingyelirun", "op");
                    transfer(map, jsonArrayJSONObject, "jia_yingyewaishouru", "non_operating_income");
                    transfer(map, jsonArrayJSONObject, "jian_yingyewaichichu", "non_operating_payout");
                    transfer(map, jsonArrayJSONObject, "lirunzonge", "profit_total_amt");
                    transfer(map, jsonArrayJSONObject, "jian_suodeshuifeiyong", "income_tax_expenses");
                    transfer(map, jsonArrayJSONObject, "jinglirun", "net_profit");
                    transfer(map, jsonArrayJSONObject, "guishuyumugongsigudongdejinglirun", "net_profit_atsopc");
                    transfer(map, jsonArrayJSONObject, "shaoshugudongquanyi", "minority_gal");
                    transfer(map, jsonArrayJSONObject, "kouchufeijingchangxingsuiyihoudejinglirun", "net_profit_after_nrgal_atsolc");

                    transfer(map, jsonArrayJSONObject, "jibeimeigushouyi", "basic_eps");
                    transfer(map, jsonArrayJSONObject, "xishimeigushouyi", "dlt_earnings_per_share");
                    transfer(map, jsonArrayJSONObject, "qitazongheshouyi", "othr_compre_income");
                    transfer(map, jsonArrayJSONObject, "guishumugongsisuoyouzhedeqitazonghequanyi", "othr_compre_income_atoopc");
                    transfer(map, jsonArrayJSONObject, "guishuyushaoshugudongdeqitazongheshouyi", "othr_compre_income_atms");
                    transfer(map, jsonArrayJSONObject, "zongheshouyizonge", "total_compre_income");
                    transfer(map, jsonArrayJSONObject, "guishuyumugongsigudongdezongheshouyizonge", "total_compre_income_atsopc");
                    transfer(map, jsonArrayJSONObject, "guishuyushaoshugudongdezongheshouyizonge", "total_compre_income_atms");
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
