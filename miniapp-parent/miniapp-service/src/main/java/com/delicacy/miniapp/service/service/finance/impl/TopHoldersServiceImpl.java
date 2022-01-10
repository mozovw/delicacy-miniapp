package com.delicacy.miniapp.service.service.finance.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import com.delicacy.miniapp.service.service.finance.TopHoldersSerivice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author yutao
 * @create 2022-01-08 22:13
 **/
@Slf4j
@Service
public class TopHoldersServiceImpl extends AbstractService implements TopHoldersSerivice {
    @Autowired
    private BaseDataService baseDataService;
    private final static String FINANCE_TOP_HOLDERS ="finance_top_holders";
    @Override
    public void runTask() {
        final List<Map> mapList = financeTopHolders();
        initData(FINANCE_TOP_HOLDERS,mapList);
    }

    @Override
    public PageResult<Map> page(Map params) {
        return getMapPageResult(params, FINANCE_TOP_HOLDERS);
    }

    private List<Map> financeTopHolders(){
        List<Map> mapList = new ArrayList<>();
        int n = 6;
        // 2年内的 季报，年报，半年报
        DateTime offset = DateTime.now();
        List<String> yyyyList = new ArrayList<>();
        yyyyList.addAll(getYYYYList(offset));
        offset = DateUtil.offset(offset, DateField.YEAR, -1);
        yyyyList.addAll(getYYYYList(offset));
        List<String> lists = Arrays.asList(yyyyList.toArray(new String[0]));

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(lists)
        ));
        List<Map> list =   baseDataService.list("xueqiu_astock_top_holders",query);
        Map<Object, List<Map>> symbolMap = list.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));
        symbolMap.entrySet().forEach(e -> {
            final String symbol = e.getKey().toString();
            List<Map> maps = e.getValue();
            int size = maps.size();
            if (size >= 10) {
                // 排序 倒序
                final Map<String, List<Map>> reportDateMap = maps.stream().collect(Collectors.groupingBy(ee -> ee.get("report_date").toString()));
                reportDateMap.entrySet().stream().sorted((a, b) -> {
                    Long aa = getLastTimestamp(a.getKey());
                    Long bb = getLastTimestamp(b.getKey());
                    return bb.compareTo(aa);
                });
                reportDateMap.entrySet().stream().findFirst().ifPresent(ee->{
                    final List<Map> value = ee.getValue();
                    if (value.stream().sorted(Comparator.comparing(eee -> getDouble(eee.get("chigubili")))).unordered().limit(2).anyMatch(eee->isEmpty(eee.get("jiaoshangqibiandong")))) {
                        return;
                    }
                    if (value.stream().filter(eee->isEmpty(eee.get("jiaoshangqibiandong"))).distinct().count()>5) {
                        return;
                    }

                    final double sum = value.stream().mapToDouble(eee -> {
                        final Object jiaoshangqibiandong = eee.get("jiaoshangqibiandong");
                        final Object chigubili = eee.get("chigubili");
                        if (isEmpty(chigubili)) {
                            return getDouble("0");
                        }
                        if (isEmpty(jiaoshangqibiandong)){
                            return getDouble(eee.get("chigubili"));
                        }
                        if (getDouble(jiaoshangqibiandong)==0){
                            return getDouble("0");
                        }
                        final String chigubiliStr = getString(chigubili, n);
                        final String jiaoshangqibiandongStr = getStringDiv( getString(jiaoshangqibiandong, n),100,n);
                        final Double v = getDouble(jiaoshangqibiandongStr) + 1;
                        final String stringDiv = getStringDiv(chigubiliStr, v, n);
                        final String stringMul = getStringMul(stringDiv, jiaoshangqibiandongStr,n);
                        final String string = getString(stringMul, 3);
                        return getDouble(string);
                    }).sum();
                    if (sum<=0){
                        return;
                    }
                    Map<String,Object> map = new LinkedHashMap<>();
                    map.put("symbol",symbol);
                    map.put("reportDate",ee.getKey());
                    map.put("biandongbili",getString(sum,3));
                    mapList.add(map);

                });
            }
        });

        final String[] symbols = mapList.stream().map(e -> e.get("symbol").toString()).distinct().toArray(String[]::new);
        final List<Map> maps = baseDataService.listStock(symbols);

        final List<Map> allMap = addAllMap(mapList, maps);
        final List<Map> collect = allMap.stream().filter(e-> getDouble(e.get("shiyinglv_TTM"))>0).sorted((a, b) -> {
            final Double peg = getDouble(a.get("biandongbili"));
            final Double peg1 = getDouble(b.get("biandongbili"));
            return peg1.compareTo(peg);
        }).collect(Collectors.toList());

        return collect;

    }




}
