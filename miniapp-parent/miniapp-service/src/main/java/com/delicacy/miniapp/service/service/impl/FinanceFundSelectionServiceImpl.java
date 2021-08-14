package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.delicacy.common.utils.BigDecimalUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.AnalysisFundRankPositionService;
import com.delicacy.miniapp.service.service.AnalysisStockService;
import com.delicacy.miniapp.service.service.FinanceFundSelectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:19
 **/
@Slf4j
@Service
public class FinanceFundSelectionServiceImpl extends AbstractService implements FinanceFundSelectionService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    private AnalysisFundRankPositionService analysisFundRankPositionService;

    @Autowired
    private AnalysisStockService analysisStockService;

    final static String FINANCE_FUNDSELECTION = "finance_fundselection";

    @Override
    public void runFundSelection() {
        dropCollection(FINANCE_FUNDSELECTION);
        List<List<Map>> lists = analysisFundRankPositionService.list();
        Map<String, Integer> keyValue = new HashMap<>();

        List<Map> mapList = new ArrayList<>();

        List<Map> mapList1 = lists.get(0);
        List<Map> mapList2 = lists.get(1);
        Set<String> symbol1 = mapList1.stream().map(e -> {
            String symbol = e.get("symbol").toString();
            String name = e.get("name").toString();
            String key = symbol + ":" + name;
            return key;
        }).collect(Collectors.toSet());
        Set<String> symbol2 = mapList2.stream().map(e -> {
            String symbol = e.get("symbol").toString();
            String name = e.get("name").toString();
            String key = symbol + ":" + name;
            return key;
        }).collect(Collectors.toSet());


        Collection<String> union = CollectionUtil.union(symbol1, symbol2);

        union.forEach(e -> {
            String[] split = e.split(":");
            String symbol = split[0];
            String name = split[1];
            String key = symbol + ":" + name;

            lists.subList(0,5).stream().forEach(ee -> {
                nextCalc(ee, keyValue, key);
            });
            Integer sum = keyValue.get(key);

            String huanbi_bilv_5 = getHuanbiBilv5(lists, key);

            String huanbi_bilv = getHuanbiBilv(lists, key);


            if (sum != null&&huanbi_bilv!=null&&huanbi_bilv_5!=null) {
                LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
                map.put("symbol", symbol);
                map.put("name", name);
                map.put("huanbi_bilv", huanbi_bilv);
                map.put("huanbi_bilv_5", huanbi_bilv_5);
                map.put("sum", sum);
                mapList.add(map);
            }
        });

        String[] symbols = mapList.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new);
        List<Map> maps3 = analysisStockService.list(symbols);
        if (isEmpty(maps3)) {
            return;
        }
        List<Map> maps = addAllMap(maps3, mapList);
        maps.stream().sorted(Comparator.comparing(e -> -Integer.parseInt(e.get("sum").toString()))).forEach(e -> {
            addData(e, FINANCE_FUNDSELECTION);
        });
    }

    private String getHuanbiBilv5(List<List<Map>> lists, String key) {
        Map<String, Integer> keyValue = new LinkedHashMap<>();
        lists.subList(0,5).stream().forEach(ee -> {
            nextCalc(ee, keyValue, key);
        });
        Integer count1 = keyValue.get(key);

        lists.subList(5,10).stream().forEach(ee -> {
            nextCalc(ee, keyValue, key);
        });
        Integer count2 = keyValue.get(key);

        String huanbi_bilv_5;
        if (count1!=null && count2!=null) {
            huanbi_bilv_5 = BigDecimalUtils.div((count1 - count2), count2).toString();
        } else {
            huanbi_bilv_5 = "0";
        }
        return huanbi_bilv_5;
    }

    private String getHuanbiBilv(List<List<Map>> lists, String key) {
        Optional<Map> first = lists.get(0).stream().filter(
                ee -> {
                    String sm = ee.get("symbol").toString() + ":" + ee.get("name").toString();
                    return key.equals(sm);
                }
        ).findFirst();
        Optional<Map> second = lists.get(1).stream().filter(
                ee -> {
                    String sm = ee.get("symbol").toString() + ":" + ee.get("name").toString();
                    return key.equals(sm);
                }
        ).findFirst();
        String huanbi_bilv;
        if (second.isPresent() && first.isPresent()) {
            Integer count1 = Integer.parseInt(first.get().get("count").toString());
            Integer count2 = Integer.parseInt(second.get().get("count").toString());
            huanbi_bilv = BigDecimalUtils.div((count1 - count2), count2).toString();
        } else {
            huanbi_bilv = "0";
        }
        return huanbi_bilv;
    }

    private void nextCalc(List<Map> maps, Map<String, Integer> keyValue, Object key) {
        maps.stream().filter(
                ee -> {
                    String symbol = ee.get("symbol").toString();
                    String name = ee.get("name").toString();
                    String sm = symbol + ":" + name;
                    return key.equals(sm);
                }
        ).forEach(ee -> {
            String symbol = ee.get("symbol").toString();
            String name = ee.get("name").toString();
            String count = ee.get("count").toString();
            String sm = symbol + ":" + name;
            Integer sum = keyValue.get(sm);
            keyValue.put(sm, sum == null ? Integer.parseInt(count) : sum + Integer.parseInt(count));
        });
    }


    @Override
    public PageResult<Map> pageFundSelection(Map params) {
        PageResult<Map> mapPageResult = getMapPageResult(params, FINANCE_FUNDSELECTION);
        return mapPageResult;
    }


}
