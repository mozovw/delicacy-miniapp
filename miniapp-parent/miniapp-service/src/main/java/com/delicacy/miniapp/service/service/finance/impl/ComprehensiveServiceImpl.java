package com.delicacy.miniapp.service.service.finance.impl;

import com.delicacy.miniapp.service.entity.ControlParam;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.*;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import com.delicacy.miniapp.service.service.finance.ComprehensiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:19
 **/
@Slf4j
@Service
public class ComprehensiveServiceImpl extends AbstractService implements ComprehensiveService {

    final static String FINANCE_COMPREHENSIVE = "finance_comprehensive";

    @Override
    public void runTask() {

        final Map<String, Map> stockReportMap = stockReport().stream().collect(Collectors.toMap(e -> e.get("symbol").toString(), Function.identity()));

        final Map<String, Map> fundRankPositionMap = fundRankPosition().stream().collect(Collectors.toMap(e -> e.get("symbol").toString(), Function.identity()));

        final Map<String, Map> stockMap = stock().stream().collect(Collectors.toMap(e -> e.get("symbol").toString(), Function.identity()));

        List<Map> mapList = new ArrayList<>();
        fundRankPositionMap.forEach((key, value) -> {
            if (Integer.parseInt(value.get("count").toString()) >= 10) {
                final Map map = stockReportMap.get(key);
                final Map map1 = stockMap.get(key);
                if (!isEmpty(map)&&!isEmpty(map1)){
                    mapList.add(addAllMap(addAllMap(map, value),map1));
                }
            }
        });
        final List<Map> listStock = baseDataService.listStock(mapList.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new));
        final List<Map> resultList = addAllMap(mapList, listStock);
        resultList.sort(Comparator.comparing(e->-Long.parseLong(e.get("count").toString())));
        initData(FINANCE_COMPREHENSIVE,resultList);
    }


    @Override
    public PageResult<Map> page(Map params) {
        return getMapPageResult(params,FINANCE_COMPREHENSIVE);
    }


    public List<Map> stock() {
        List<Map> mapList = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("$where").is("this.shiyinglv_TTM * 1 < this.shiyinglv_jing * 1"),
                Criteria.where("$where").is("this.shiyinglv_TTM > 0"),
                Criteria.where("$where").is("this.shiyinglv_dong * 1 < this.shiyinglv_jing * 1"),
                Criteria.where("$where").is("this.shiyinglv_TTM < 60")
        ));

        List<Map> maps =  baseDataService.list("xueqiu_astock",query);

        maps.stream().filter(e -> {
            try {
                // Double guxilv_TTM = percentData(e.get("guxilv_TTM").toString());
                Double zongshizhi = moneyData(e.get("zongshizhi").toString());
                return zongshizhi > 2000000000L; //&& guxilv_TTM > 0.5;//50亿
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).forEach(mapList::add);
        return mapList;
    }


    public List<Map> fundRankPosition() {
        List<Map> mapList = new ArrayList<>();
        //  近1月赢利最多的基金
        Query query = new Query();
        final String type = "jin1yue";
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("type").in("gpx", "hhx", "ETF").
                        and(type).ne("")
        ));
        List<Map> list =   baseDataService.list("aijijin_fund_rank",query);
        Collections.sort(list, Comparator.comparing(e -> Double.valueOf((String) ((Map) e).get(type))).reversed());

        List<Object> symbol = list.stream().map(e -> e.get("symbol")).limit(1000).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("fund_code").in(symbol)
        ));
        List<Map> maps = baseDataService.list("aijijin_fund_position",query);

        //  分组排序 根据股票代码分组，占净值比例>4
        Map<Object, List<Map>> collect = maps.stream().filter(e -> {
            try {
                if (e.get("symbol") == null) {
                    return false;
                }
                Double zhanjingzhibili = percentData(e.get("zhanjingzhibi").toString());
                return zhanjingzhibili > 0;
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).collect(Collectors.groupingBy(e ->
                e.get("symbol")
        ));
        //  显示结果 股票代码 股票名称 买入总个数
        collect.entrySet().forEach(e -> {
            int size = e.getValue().size();
            if (size >= 5) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("symbol", e.getKey());
                map.put("name", e.getValue().get(0).get("gupiaomingcheng"));
                map.put("count", String.valueOf(size));
                mapList.add(map);
            }
        });
        return mapList;
    }



    public List<Map> stockReport() {

        List<Map> mapList = new ArrayList<>();

        List<String> reportList = getReportList();
        String data1 = reportList.get(0);
        String data2 = reportList.get(1);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(data1, data2)
        ));

        Map<Object, List<Map>> collect = baseDataService.list("xueqiu_astock_report",query)
                .stream().collect(Collectors.groupingBy(e ->
                e.get("symbol")
        ));

        collect.entrySet().stream().forEach(e -> {
            List<Map> maps = e.getValue();
            int size = maps.size();
            if (size == 2) {
                Map map1, map2;
                if (maps.get(0).get("report_date").toString().contains(data1)) {
                    map1 = maps.get(0);
                    map2 = maps.get(1);
                } else {
                    map1 = maps.get(1);
                    map2 = maps.get(0);
                }
                List<ControlParam> list = new ArrayList<>();
                list.add(new ControlParam("yingyeshouru", 1, true));
                list.add(new ControlParam("jinglirun", 1, true));
                list.add(new ControlParam("meigushouyi", 1, true));
                list.add(new ControlParam("jingzichanshouyilv", 1, true));
                list.add(new ControlParam("renlitouruhuibaolv", 2, true));
                list.add(new ControlParam("xiaoshoumaolilv", 2, true));
                list.add(new ControlParam("zichanfuzhailv", 2, false));
                list.add(new ControlParam("liudongbilv", 1, true));
                list.add(new ControlParam("sudongbilv", 1, true));
                list.add(new ControlParam("cunhuozhouzhuanlv", 2, true));
                list.add(new ControlParam("yingyezhouqi", 2, false));
                list.add(new ControlParam("xianjinxunhuanzhouqi", 2, false));

                Boolean result = calcResult(map1, map2, list);
                if (result) {
                    mapList.add(map1);
                }
            }
        });
        return mapList;
    }


    private Boolean calcResult(Map map1, Map map2, List<ControlParam> list) {
        boolean flag = true;
        AtomicInteger mustRealCount = new AtomicInteger(0);
        AtomicInteger mustCount = new AtomicInteger(0);
        AtomicInteger mayRealCount = new AtomicInteger(0);
        AtomicInteger mayCount = new AtomicInteger(0);

        list.forEach(e -> {
            String key = e.getKey();

            if (ObjectUtils.isEmpty(map1.get(key)) || ObjectUtils.isEmpty(map2.get(key))) {
                return;
            }

            String o1 = map1.get(key).toString();
            String o2 = map2.get(key).toString();
            if (o1.contains("-")) {
                mustCount.incrementAndGet();
                mayCount.incrementAndGet();
                return;
            }
            if (o2.contains("-")) {
                return;
            }
            if (e.getWeigth() == 1) {
                if (e.getDirect() && getDouble(o1) * (flag ? 1 : 0.7) >= getDouble(o2)) {
                    mustRealCount.incrementAndGet();
                } else if (!e.getDirect() && getDouble(o1) * (flag ? 1 : 1.3) <= getDouble(o2)) {
                    mustRealCount.incrementAndGet();
                }
                mustCount.incrementAndGet();
            } else {
                if (e.getDirect() && getDouble(o1) * (flag ? 1.05 : 1) >= getDouble(o2)) {
                    mayRealCount.incrementAndGet();
                } else if (!e.getDirect() && getDouble(o1) * (flag ? 0.95 : 1) <= getDouble(o2)) {
                    mayRealCount.incrementAndGet();
                }
                mayCount.incrementAndGet();
            }
        });
        if (mustRealCount.intValue() == mustCount.intValue()) {
            if (mayCount.intValue() - (flag ? mayCount.intValue() : 0) <= mayRealCount.intValue()) {
                return true;
            }
        }

        return false;
    }

    private Double getDouble(String val) {
        if (val.contains("次")) {
            return numData(val);
        } else if (val.contains("%")) {
            return percentData(val);
        } else if (val.contains("天")) {
            return dayData(val);
        } else {
            return moneyData(val);
        }

    }


}
