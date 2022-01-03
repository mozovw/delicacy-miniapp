package com.delicacy.miniapp.service.service.analysis.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.entity.ControlParam;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.analysis.StockReportService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-07-28 15:21
 **/
@Service
public class StockReportServiceImpl extends AbstractService implements StockReportService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Override
    public List<Map> list(String... symbols) {
        stock_report();
        Query query = new Query();
        if (!isEmpty(symbols)) {
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("symbol").in(symbols)
            ));
        }
        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_astock_report");
        return maps;
    }

    protected List<String> getReportList() {
        DateTime now = DateTime.now();
        String format = DateUtil.format(now, "yyyy");
        int year = Integer.parseInt(format);

        List<String> list = null;
        for (int i = year; i > 0; i--) {
            String s = i + "年报";
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "年报");
                break;
            }

            s = i + "三季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "三季报");
                break;
            }

            s = i + "中报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "中报");
                break;
            }

            s = i + "一季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "一季报");
                break;
            }
        }
        return list;

    }

    public void stock_report() {
        String analysis_table = "analysis_astock_report";
        dropCollection(analysis_table);

        List<String> reportList = getReportList();
        String data1 = reportList.get(0);
        String data2 = reportList.get(1);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(data1, data2)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "xueqiu_astock_report");

        Map<Object, List<Map>> collect = maps.stream().collect(Collectors.groupingBy(e ->
                e.get("symbol")
        ));

        ArrayList<String> objects = Lists.newArrayList();
        collect.entrySet().stream().forEach(e -> {
            List<Map> mapList = e.getValue();
            int size = mapList.size();
            if (size == 2) {
                Map map1, map2;
                if (mapList.get(0).get("report_date").toString().contains(data1)) {
                    map1 = mapList.get(0);
                    map2 = mapList.get(1);
                } else {
                    map1 = mapList.get(1);
                    map2 = mapList.get(0);
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
                    objects.add(String.format("%s_%s_%s",
                            e.getKey(), map1.get("name"),
                            map1.get("report_date")));
                    addData(map1, analysis_table);
                }

            }
        });
//        objects.forEach(System.out::println);

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
