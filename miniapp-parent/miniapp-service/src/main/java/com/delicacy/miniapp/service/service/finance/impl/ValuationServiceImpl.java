package com.delicacy.miniapp.service.service.finance.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.common.utils.BigDecimalUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import com.delicacy.miniapp.service.service.finance.ValuationService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ValuationServiceImpl extends AbstractService implements ValuationService {

    final static String FINANCE_VALUATION = "finance_valuation";


    @Override
    public void runTask() {
        final List<Map> mapList = financeValuation();
        initData(FINANCE_VALUATION, mapList);
    }

    @Override
    public PageResult<Map> page(Map params) {
        PageResult<Map> mapPageResult = getMapPageResult(params, FINANCE_VALUATION);
        return mapPageResult;
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
    public List<Map> financeValuation() {
        // 4年内的 季报，年报，半年报
        DateTime offset = DateTime.now();
        List<String> yyyyList = new ArrayList<>();
        yyyyList.addAll(getYYYYList(offset));
        offset = DateUtil.offset(offset, DateField.YEAR, -1);
        yyyyList.addAll(getYYYYList(offset));
        offset = DateUtil.offset(offset, DateField.YEAR, -1);
        yyyyList.addAll(getYYYYList(offset));
        offset = DateUtil.offset(offset, DateField.YEAR, -1);
        yyyyList.addAll(getYYYYList(offset));
        List<String> lists = Arrays.asList(yyyyList.toArray(new String[0]));


        final List<String> symbolList = baseDataService.allSymbol();

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(lists)
        ));
        List<Map> flowReport =   baseDataService.list("xueqiu_astock_cash_flow_report",query);

        Map<Object, List<Map>> symbolCashFlowReportMap = flowReport.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));


        List<Map> allList = new ArrayList<>();

        symbolCashFlowReportMap.entrySet().forEach(e -> {
            List<Map> mapList = e.getValue();
            int size = mapList.size();
            if (size >= 5) {
                // 排序 倒序
                mapList.sort((a, b) -> {
                    Long aa = getLastTimestamp(a.get("report_date").toString());
                    Long bb = getLastTimestamp(b.get("report_date").toString());
                    return bb > aa ? 1 : -1;
                });

                Optional<Map> reportOptional = mapList.stream().filter(ee -> ee.get("report_date").toString().contains("年报")).findFirst();
                if (!reportOptional.isPresent()) {
                    return;
                }
                Map report = reportOptional.get();
                // 年报的期末现金流
                final Object qimoxianjinjixianjindengjiawuyue = report.get("qimoxianjinjixianjindengjiawuyue");
                final Object xianjinjixianjindengjiawujingzengjiae = report.get("xianjinjixianjindengjiawujingzengjiae");
                if (qimoxianjinjixianjindengjiawuyue==null||xianjinjixianjindengjiawujingzengjiae==null){
                    return;
                }
                String qimo_xianjinliu = qimoxianjinjixianjindengjiawuyue.toString();
                String qimo_qichu_xianjinliu_chae = xianjinjixianjindengjiawujingzengjiae.toString();
                if (isEmpty(qimo_xianjinliu) && isEmpty(qimo_qichu_xianjinliu_chae) && Double.parseDouble(qimo_qichu_xianjinliu_chae) <= 0) {
                    return;
                }
                BigDecimal zengzhanglv = BigDecimalUtils.div(qimo_qichu_xianjinliu_chae, qimo_xianjinliu);

                reportOptional = mapList.stream().findFirst();
                if (!reportOptional.isPresent()) {
                    return;
                }
                report = reportOptional.get();
                // 最新的季度
                String qimo_xianjinliu_zuixin = report.get("qimoxianjinjixianjindengjiawuyue").toString();
                String qimo_qichu_xianjinliu_chae_zuixin = report.get("xianjinjixianjindengjiawujingzengjiae").toString();

                if (isEmpty(qimo_xianjinliu_zuixin) && isEmpty(qimo_qichu_xianjinliu_chae_zuixin) && Double.parseDouble(qimo_qichu_xianjinliu_chae_zuixin) <= 0) {
                    return;
                }
                BigDecimal zengzhanglv_zuixin_bigdecimal = BigDecimalUtils.div(qimo_qichu_xianjinliu_chae_zuixin, qimo_xianjinliu_zuixin);
                Double zengzhanglv_zuixin = Double.parseDouble(zengzhanglv_zuixin_bigdecimal.setScale(3).toString());
                if (!ObjectUtils.isEmpty(zengzhanglv)
                        && zengzhanglv.scale() > 0
                        && mapList.stream().limit(5).allMatch(ee ->
                        !ObjectUtils.isEmpty(ee.get("xianjinjixianjindengjiawujingzengjiae"))
                                && Double.parseDouble(ee.get("xianjinjixianjindengjiawujingzengjiae").toString()
                        ) > 0)) {
                    String s = e.getKey().toString();
                    Double zengzhanglv_average = getAverage(mapList, "xianjinjixianjindengjiawujingzengjiae", "qimoxianjinjixianjindengjiawuyue", 10);

                    // 最新的营业收入和净利润必须大于平均
                    if (zengzhanglv_average < 0 || zengzhanglv_zuixin < zengzhanglv_average) {
                        return;
                    }
                    // 估值计算
                    String s1 = valueCalc(qimo_xianjinliu, "0.08", String.valueOf(zengzhanglv_average), String.valueOf(zengzhanglv_average / 2.0001), 3);
                    if (symbolList.contains(e.getKey().toString())){
                        Map<String,Object> linkedHashMap = new LinkedHashMap();
                        linkedHashMap.put("symbol", s);
                        linkedHashMap.put("report_date", mapList.get(0).get("report_date"));
                        linkedHashMap.put("xianjinliu_zongshizhi", getString(s1));
                        allList.add(linkedHashMap);
                    }
                }
            }
        });
        final String[] symbols = allList.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new);
        final List<Map> maps = baseDataService.listStock(symbols);
        final List<Map> mapList = addAllMap(maps, allList);
        final List<Map> collect = mapList.stream().filter(e -> {
            Double xianjinliu_current = getDouble(e.get("xianjinliu_zongshizhi"));
            Double current = getDouble(e.get("zongshizhi"));
            return xianjinliu_current > current;
        }).peek(e -> {
            e.put("xianjinliu_current", getStringDiv(e.get("xianjinliu_zongshizhi"),e.get("zongguben") ));
            Double xianjinliu_current = getDouble(e.get("xianjinliu_current"));
            Double current = getDouble(e.get("current"));
            String s = new BigDecimal(xianjinliu_current).divide(new BigDecimal(current), RoundingMode.HALF_DOWN).setScale(1, RoundingMode.HALF_DOWN).toString();
            e.put("gushibi", s);
        }).sorted((a, b) -> {
            Double a_xianjinliu_current = getDouble(a.get("gushibi"));
            Double b_xianjinliu_current = getDouble(b.get("gushibi"));
            return a_xianjinliu_current > b_xianjinliu_current ? -1 : 1;
        }).collect(Collectors.toList());

        return collect;
    }






    private double getAverage(List<Map> mapList, String a, String b, int limit) {
        List<String> resutltList = mapList.stream().filter(ee -> ee.get(a) != null && ee.get(b) != null).map(e -> BigDecimalUtils.div(e.get(a), e.get(b)).setScale(3).toString()).collect(Collectors.toList());
        List<String> stringList = resutltList.stream().sorted(Comparator.comparing(Double::parseDouble)).skip(1).limit(limit - 2).collect(Collectors.toList());
        return stringList.stream().mapToDouble(Double::parseDouble).average().getAsDouble();
    }


    private String valueCalc(String freeMoney, String tiexianlv, String zengzhanglv, String zengzhanglv_yihou, int year) {
        if (Double.parseDouble(tiexianlv) < Double.parseDouble(zengzhanglv_yihou)) {
            // 贴现率应该大于年后增长率
            zengzhanglv_yihou = (Double.parseDouble(tiexianlv) - 0.01) + "";
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
