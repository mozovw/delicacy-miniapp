package com.delicacy.miniapp.service.service.finance.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import com.delicacy.miniapp.service.service.finance.SkHolderSerivice;
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
public class SkHolderServiceImpl extends AbstractService implements SkHolderSerivice {
    private final static String FINANCE_SK_HOLDER = "finance_sk_holder";

    @Override
    public void runTask() {
        final List<Map> mapList = financeSkHolder();
        initData(FINANCE_SK_HOLDER, mapList);
    }

    @Override
    public PageResult<Map> page(Map params) {
        return getMapPageResult(params, FINANCE_SK_HOLDER);
    }

    private List<Map> financeSkHolder() {
        List<Map> mapList = new ArrayList<>();
        int n = 6;
        // 2年内的 季报，年报，半年报
        DateTime offset = DateTime.now();
        final DateTime dateTime = DateUtil.offsetMonth(offset, -6);
        final String formatDate = DateUtil.formatDate(dateTime);

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("biandongriqi").gte(formatDate)
        ));
        List<Map> list =   baseDataService.list("xueqiu_astock_sk_holder_chg",query);
        Map<Object, List<Map>> symbolMap = list.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));
        symbolMap.forEach((key, maps) -> {
            final String symbol = key.toString();
            if (maps.stream().sorted(Comparator.comparing(e-> DateUtil.parseDate(e.get("biandongriqi").toString()).getTime())).unordered().limit(2)
                    .anyMatch(e-> getDouble(e.get("biandonggushu"))<0)) {
                return;
            }
            final long gaoguan = maps.stream().map(e -> e.get("mingcheng").toString()).distinct().count();
            if (gaoguan<3){
                return;
            }

            final Map firstMap = maps.stream().sorted(Comparator.comparing(e -> DateUtil.parseDate(e.get("biandongriqi").toString()).getTime())).unordered().findFirst().get();
            final double sum = maps.stream().sorted(Comparator.comparing(e-> DateUtil.parseDate(e.get("biandongriqi").toString()).getTime())).unordered().limit(7).mapToDouble(ee -> {
                final Object biandonggushu = ee.get("biandonggushu");
                final Object junjia = ee.get("junjia");
                if (isEmpty(biandonggushu) || isEmpty(junjia)) {
                    return 0.0;
                }
                final String stringMul = getStringMul(biandonggushu, junjia);
                return getDouble(stringMul);
            }).sum();
            if (sum <= 0) {
                return;
            }
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("symbol", symbol);
            map.put("xinbianriqi", firstMap.get("biandongriqi"));
            map.put("jincibiandongzongzhi", sum);
            mapList.add(map);
        });

        final String[] symbols = mapList.stream().map(e -> e.get("symbol").toString()).distinct().toArray(String[]::new);
        final List<Map> maps = baseDataService.listStock(symbols);

        final List<Map> allMap = addAllMap(mapList, maps);
        final List<Map> collect = allMap.stream().filter(e -> getDouble(e.get("shiyinglv_TTM")) > 0).sorted((a, b) -> {
            final Double peg = getDouble(a.get("jincibiandongzongzhi"));
            final Double peg1 = getDouble(b.get("jincibiandongzongzhi"));
            return peg1.compareTo(peg);
        }).collect(Collectors.toList());

        return collect;

    }


}
