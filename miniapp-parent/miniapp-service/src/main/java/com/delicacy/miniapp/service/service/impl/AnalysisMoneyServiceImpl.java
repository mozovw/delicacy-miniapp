package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.AnalysisMoneyService;
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
public class AnalysisMoneyServiceImpl extends AbstractService implements AnalysisMoneyService {

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
     * 获取两年财报数据，季报，年报，半年报
     * 计算：财报中 yingyeshourutongbizengzhang jingliruntongbizengzhang
     * 计算规则：
     * 至少5个
     * 最近三个正值
     * 去掉最大，去掉最小，求平均
     */
    // 一月一号---四月三十号，每个星期执行一次
    public void astock_money() {
        String analysis_table = ANALYSIS_ASTOCK_MONEY;
        dropCollection(analysis_table);

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
        Map<String, Map> stockMap = mapStocks.stream().collect(Collectors.toMap(e -> e.get("symbol").toString().replace("SH", "").replace("SZ", ""), e -> e));


        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(lists)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "xueqiu_astock_report");


        Map<Object, List<Map>> collect = maps.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));


        List<Map> mapReportList = new ArrayList<>();

        collect.entrySet().forEach(e -> {
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
                if (!reportOptional.isPresent()) return;
                Map report = reportOptional.get();
                // 年报的净利润
                String jinglirun = report.get("jinglirun").toString();

                reportOptional = mapList.stream().findFirst();
                if (!reportOptional.isPresent()) return;
                report = reportOptional.get();
                // 最新的季度
                Object jingliruntongbizengzhang1 = report.get("jingliruntongbizengzhang");
                if(isEmpty(jingliruntongbizengzhang1))return;
                Double jingliruntongbizengzhang_report = Double.parseDouble(jingliruntongbizengzhang1.toString());

                if (!ObjectUtils.isEmpty(jinglirun)
                        && Double.parseDouble(jinglirun) > 0
                        && mapList.stream().limit(5).allMatch(ee -> !ObjectUtils.isEmpty(ee.get("jingliruntongbizengzhang"))
                        && Double.parseDouble(ee.get("jingliruntongbizengzhang").toString()) > 0)) {
                    String s = e.getKey().toString();
                    double jingliruntongbizengzhang = getAverage(mapList, "jingliruntongbizengzhang", 5);
                    double yingyeshourutongbizengzhang = getAverage(mapList, "yingyeshourutongbizengzhang", 5);
                    // 最新的营业收入和净利润必须大于平均
                    if (jingliruntongbizengzhang < 0 || yingyeshourutongbizengzhang < 0 || jingliruntongbizengzhang_report < jingliruntongbizengzhang)
                        return;
                    // 估值计算
                    String s1 = valueCalc(jinglirun, "0.08", String.valueOf(jingliruntongbizengzhang / 100), String.valueOf(jingliruntongbizengzhang / 200), 3);
                    String s2 = valueCalc(jinglirun, "0.08", String.valueOf(yingyeshourutongbizengzhang / 100), String.valueOf(yingyeshourutongbizengzhang / 200), 3);
                    Map map = stockMap.get(e.getKey().toString());
                    if (map == null) return;
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
                    linkedHashMap.put("yy_zongshizhi", getString(s2));
                    linkedHashMap.put("yy_current", getString(s2, zongguben));
                    linkedHashMap.put("jl_zongshizhi", getString(s1));
                    linkedHashMap.put("jl_current", getString(s1, zongguben));
                    mapReportList.add(linkedHashMap);
                }
            }
        });


//        FileOutputhandler.builder()
//                .minSize( 512 * 1024)
//                .subDir("guzhi")
//                .fileName("guzhi")
//                .path("D:\\data\\")
//                .subffix("md").build().writer(map);
        mapReportList.forEach(e -> addData(e, analysis_table));
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


    private String valueCalc(String freeMoney, String tiexianlv, String zengzhanglv, String zengzhanglv_yihou, int year) {
        if (Double.parseDouble(tiexianlv) < Double.parseDouble(zengzhanglv_yihou)) {
            zengzhanglv_yihou = (Double.parseDouble(tiexianlv) - 0.01) + "";
//            throw new RuntimeException("贴现率应该大于年后增长率");
        }
        //todo 前十年贴现
        BigDecimal sum_zhexian = new BigDecimal(0);
        BigDecimal ziyouxianjin = new BigDecimal(0);
        for (int i = 0; i < year; i++) {
            ziyouxianjin = new BigDecimal(freeMoney).multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv)).pow(i + 1));
            BigDecimal zhexian = ziyouxianjin.divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(i + 1), RoundingMode.HALF_DOWN);
            sum_zhexian = zhexian.add(sum_zhexian);
        }
        //todo 十年之后贴现
        BigDecimal ziyouxianjin_yihou = ziyouxianjin.multiply(BigDecimal.valueOf(1 + Double.parseDouble(zengzhanglv_yihou)))
                .multiply(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv))
                        .divide(BigDecimal.valueOf(Double.parseDouble(tiexianlv) - Double.parseDouble(zengzhanglv_yihou)), RoundingMode.HALF_DOWN));
        BigDecimal sum_zhexian_yihou = ziyouxianjin_yihou
                .divide(BigDecimal.valueOf(1 + Double.parseDouble(tiexianlv)).pow(year + 1), RoundingMode.HALF_DOWN);
        String sum = sum_zhexian_yihou.add(sum_zhexian).setScale(1, RoundingMode.HALF_DOWN).toString();
        return sum;
    }


}
