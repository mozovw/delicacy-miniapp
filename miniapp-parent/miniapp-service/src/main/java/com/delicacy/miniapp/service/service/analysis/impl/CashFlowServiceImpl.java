package com.delicacy.miniapp.service.service.analysis.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.common.utils.BigDecimalUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.analysis.CashFlowService;
import com.delicacy.miniapp.service.service.analysis.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-07-28 15:21
 **/
@Service
public class CashFlowServiceImpl extends AbstractService implements CashFlowService {

    @Autowired
    protected MongoTemplate mongoTemplate;
    final static String ANALYSIS_ASTOCK_MONEY = "analysis_astock_money";

    @Override
    public List<Map> list(String... symbols) {
        astock_money();
        Query query = new Query();
        if (!isEmpty(symbols)) {
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("symbol").in(symbols)
            ));
        }
        List<Map> maps = mongoTemplate.find(query, Map.class, ANALYSIS_ASTOCK_MONEY);
        return maps;
    }



    private Long getTimestamp(String date) {
        String year = date.substring(0, 4);
        String report = date.substring(4);
        String ymd = year;
        switch (report) {
            case "一季报":
                ymd += "-03-31";
                break;
            case "中报":
                ymd += "-06-30";
                break;
            case "三季报":
                ymd += "-09-30";
                break;
            case "年报":
                ymd += "-12-31";
                break;
            default:
                throw new RuntimeException();
        }
        DateTime parse = DateUtil.parse(ymd, "yyyy-MM-dd");
        return parse.getTime();
    }


    /**
     * 基于现金流量表： 期末现金及现金等价物余额、现金及现金等价物净增加额
     * 获取两年财报数据，季报，年报，半年报
     * 计算：财报中 qimoxianjinjixianjindengjiawuyue xianjinjixianjindengjiawujingzengjiae
     * 计算规则：
     * 至少5个
     * xianjinjixianjindengjiawujingzengjiae 最近三个正值
     * 去掉最大，去掉最小，求平均
     */
    public void astock_money() {
        dropCollection(ANALYSIS_ASTOCK_MONEY);

        // 3年内的 季报，年报，半年报
        DateTime offset = DateTime.now();
        List<String> yyyyList= new ArrayList<>();
        yyyyList.addAll( getYYYYList(offset));
        offset = DateUtil.offset(offset, DateField.YEAR, -1);
        yyyyList.addAll( getYYYYList(offset));
        offset = DateUtil.offset(offset, DateField.YEAR, -1);
        yyyyList.addAll( getYYYYList(offset));
        List<String> lists = Arrays.asList(yyyyList.toArray(new String[0]));

        Query query = new Query();
        List<Map> mapStocks = mongoTemplate.find(query, Map.class, "xueqiu_astock");
        Map<String, Map> stockMap = mapStocks.stream()
                .collect(Collectors.toMap(e -> e.get("symbol").toString().replace("SH", "").replace("SZ", ""), e -> e));


        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(lists)
        ));
        List<Map> cashFlowReportMapList = mongoTemplate.find(query, Map.class, "xueqiu_astock_cash_flow_report");


        Map<Object, List<Map>> symbolCashFlowReportMap = cashFlowReportMapList.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));


        List<Map> mapReportList = new ArrayList<>();

        symbolCashFlowReportMap.entrySet().forEach(e -> {
            List<Map> mapList = e.getValue();
            int size = mapList.size();
            if (size >= 5) {
                // 排序 倒序
                mapList.sort((a, b) -> {
                    Long aa = getTimestamp(a.get("report_date").toString());
                    Long bb = getTimestamp(b.get("report_date").toString());
                    return bb > aa ? 1 : -1;
                });

                Optional<Map> reportOptional = mapList.stream().filter(ee -> ee.get("report_date").toString().contains("年报")).findFirst();
                if (!reportOptional.isPresent()){
                    return;
                }
                Map report = reportOptional.get();
                // 年报的期末现金流
                String qimo_xianjinliu = report.get("qimoxianjinjixianjindengjiawuyue").toString();
                String qimo_qichu_xianjinliu_chae = report.get("xianjinjixianjindengjiawujingzengjiae").toString();
                if(isEmpty(qimo_xianjinliu)&&isEmpty(qimo_qichu_xianjinliu_chae) && Double.parseDouble(qimo_qichu_xianjinliu_chae)<=0){
                    return;
                }
                BigDecimal zengzhanglv = BigDecimalUtils.div(qimo_qichu_xianjinliu_chae, qimo_xianjinliu);

                reportOptional = mapList.stream().findFirst();
                if (!reportOptional.isPresent()){
                    return;
                }
                report = reportOptional.get();
                // 最新的季度
                String qimo_xianjinliu_zuixin = report.get("qimoxianjinjixianjindengjiawuyue").toString();
                String qimo_qichu_xianjinliu_chae_zuixin = report.get("xianjinjixianjindengjiawujingzengjiae").toString();

                if(isEmpty(qimo_xianjinliu_zuixin)&&isEmpty(qimo_qichu_xianjinliu_chae_zuixin)&& Double.parseDouble(qimo_qichu_xianjinliu_chae_zuixin) <=0){
                    return;
                }
                BigDecimal zengzhanglv_zuixin_bigdecimal = BigDecimalUtils.div(qimo_qichu_xianjinliu_chae_zuixin, qimo_xianjinliu_zuixin);
                Double zengzhanglv_zuixin = Double.parseDouble( zengzhanglv_zuixin_bigdecimal.setScale(3).toString());
                if (!ObjectUtils.isEmpty(zengzhanglv)
                        && zengzhanglv.scale() > 0
                        && mapList.stream().limit(5).allMatch(ee ->
                            !ObjectUtils.isEmpty(ee.get("xianjinjixianjindengjiawujingzengjiae"))
                            && Double.parseDouble(ee.get("xianjinjixianjindengjiawujingzengjiae").toString()
                        ) > 0)) {
                    String s = e.getKey().toString();
                    Double zengzhanglv_average = getAverage(mapList, "xianjinjixianjindengjiawujingzengjiae", "qimoxianjinjixianjindengjiawuyue",10);

                    // 最新的营业收入和净利润必须大于平均
                    if (zengzhanglv_average < 0 || zengzhanglv_zuixin < zengzhanglv_average){
                        return;
                    }
                    // 估值计算
                    String s1 = valueCalc(qimo_xianjinliu, "0.08", String.valueOf(zengzhanglv_average), String.valueOf(zengzhanglv_average/2), 3);
                    Map map = stockMap.get(e.getKey().toString());
                    if (map == null){
                        return;
                    }
                    String shiyinglv_ttm = map.get("shiyinglv_TTM").toString();
                    String zongshizhi = getString(map.get("zongshizhi"));
                    String name = map.get("name").toString();
                    String current = map.get("current").toString();
                    String zongguben = map.get("zongguben").toString();

                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    linkedHashMap.put("symbol", s);
                    linkedHashMap.put("name", name);
                    linkedHashMap.put("report_date", mapList.get(0).get("report_date"));
                    linkedHashMap.put("shiyinglv_TTM", shiyinglv_ttm);
                    linkedHashMap.put("zongguben", zongguben);
                    linkedHashMap.put("zongshizhi", zongshizhi);
                    linkedHashMap.put("current", current);
                    linkedHashMap.put("xianjinliu_zongshizhi", getString(s1));
                    linkedHashMap.put("xianjinliu_current", getString(s1, zongguben));

                    mapReportList.add(linkedHashMap);
                }
            }
        });
        
        mapReportList.forEach(e -> addData(e, ANALYSIS_ASTOCK_MONEY));
    }

    private List<String>  getYYYYList(DateTime offset) {
        List<String> yyyyList= new ArrayList<>();
        String yyyy = DateUtil.format(offset, "yyyy");
        yyyyList.add(yyyy+"年报");
        yyyyList.add(yyyy+"三季报");
        yyyyList.add(yyyy+"中报");
        yyyyList.add(yyyy+"一季报");
        return yyyyList;
    }

    private String getString(Object o) {
        if (ObjectUtils.isEmpty(o)) {
            return "0";
        }
        return new BigDecimal(o.toString()).setScale(1, RoundingMode.HALF_DOWN).toString();
    }

    private String getString(Object o, Object o1) {
        if (ObjectUtils.isEmpty(o1) || ObjectUtils.isEmpty(o)) {
            return "0";
        }
        return new BigDecimal(o.toString()).divide(new BigDecimal(o1.toString()), 1, BigDecimal.ROUND_HALF_UP).setScale(1, RoundingMode.HALF_DOWN).toString();
    }

    private double getAverage(List<Map> mapList, String s, int limit) {
        List<Map> list = mapList.stream().filter(ee -> ee.get(s) != null).limit(limit).sorted(Comparator.comparing(ee -> Double.parseDouble(ee.get(s).toString()))).collect(Collectors.toList());
        List<Map> collect = list.stream().skip(1).limit(limit - 2).collect(Collectors.toList());
        return collect.stream().mapToDouble(ee -> Double.parseDouble(ee.get(s).toString())).average().getAsDouble();
    }

    private double getAverage(List<Map> mapList, String a,String b, int limit) {
        List<String> resutltList = mapList.stream().filter(ee -> ee.get(a) != null && ee.get(b) != null).map(e -> BigDecimalUtils.div(e.get(a), e.get(b)).setScale(3).toString()).collect(Collectors.toList());
        List<String> stringList = resutltList.stream().sorted(Comparator.comparing(Double::parseDouble)).skip(1).limit(limit - 2).collect(Collectors.toList());
        return stringList.stream().mapToDouble(Double::parseDouble).average().getAsDouble();
    }


    private String valueCalc(String freeMoney, String tiexianlv, String zengzhanglv, String zengzhanglv_yihou, int year) {
        if (Double.parseDouble(tiexianlv) < Double.parseDouble(zengzhanglv_yihou)) {
            zengzhanglv_yihou = (Double.parseDouble(tiexianlv) - 0.01) + "";
//            throw new RuntimeException("贴现率应该大于年后增长率");
        }
        // 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv)).pow(i + 1));
            BigDecimal zhexian = ziyouxianjin.divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(i + 1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        // 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv_yihou)))
                .multiply(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv))
                        .divide(BigDecimal.valueOf(Double.parseDouble(tiexianlv) - Double.parseDouble(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(year + 1), RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).setScale(1, RoundingMode.HALF_DOWN).toString();
        return sum;
    }

}