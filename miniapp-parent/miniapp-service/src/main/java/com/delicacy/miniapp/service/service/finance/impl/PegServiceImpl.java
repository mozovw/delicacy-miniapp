package com.delicacy.miniapp.service.service.finance.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.common.utils.BigDecimalUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import com.delicacy.miniapp.service.service.finance.PegService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yutao
 * @create 2022-01-08 22:13
 **/
@Slf4j
@Service
public class PegServiceImpl extends AbstractService implements PegService {

    private final static String FINANCE_PEG="finance_peg";
    @Override
    public void runTask() {
        final List<Map> mapList = financePeg();
        initData(FINANCE_PEG,mapList);
    }

    @Override
    public PageResult<Map> page(Map params) {
        return getMapPageResult(params,FINANCE_PEG);
    }

    private List<Map> financePeg(){
        List<Map> mapList = new ArrayList<>();

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

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("report_date").in(lists)
        ));

        List<Map> list =   baseDataService.list("xueqiu_astock_report",query);

        Map<Object, List<Map>> symbolMap = list.stream()
                .collect(Collectors.groupingBy(e ->
                        e.get("symbol")
                ));
        symbolMap.entrySet().forEach(e -> {
            final String symbol = e.getKey().toString();
            List<Map> maps = e.getValue();
            int size = maps.size();
            if (size >= 8) {
                // 排序 倒序
                maps.sort((a, b) -> {
                    Long aa = getLastTimestamp(a.get("report_date").toString());
                    Long bb = getLastTimestamp(b.get("report_date").toString());
                    return bb > aa ? 1 : -1;
                });

                // 最新的季度
                String meigushouyi = isEmpty(maps.get(0).get("meigushouyi"))?"0":maps.get(0).get("meigushouyi").toString();
                String meigushouyi2 = isEmpty(maps.get(1).get("meigushouyi"))?"0":maps.get(1).get("meigushouyi").toString();
//                String meigushouyi3 = isEmpty(maps.get(2).get("meigushouyi"))?"0":maps.get(2).get("meigushouyi").toString();
//                String meigushouyi4 = isEmpty(maps.get(3).get("meigushouyi"))?"0":maps.get(3).get("meigushouyi").toString();
                String meigushouyi5 = isEmpty(maps.get(4).get("meigushouyi"))?"0":maps.get(4).get("meigushouyi").toString();
                String meigushouyi6 = isEmpty(maps.get(5).get("meigushouyi"))?"0":maps.get(5).get("meigushouyi").toString();
//                String meigushouyi7 = isEmpty(maps.get(6).get("meigushouyi"))?"0":maps.get(6).get("meigushouyi").toString();
//                String meigushouyi8 = isEmpty(maps.get(7).get("meigushouyi"))?"0":maps.get(7).get("meigushouyi").toString();
                if (isEmpty(meigushouyi) && isEmpty(meigushouyi2)&& isEmpty(meigushouyi5)&& isEmpty(meigushouyi6)
                    && Double.parseDouble(meigushouyi)<=0
                ) {
                    return;
                }

                if ( Double.parseDouble(meigushouyi5)>0&&Double.parseDouble(meigushouyi6)>0
                ) {
                    final BigDecimal div = BigDecimalUtils.div(BigDecimalUtils.sub(meigushouyi, meigushouyi5), meigushouyi5);
                    final BigDecimal div2 = BigDecimalUtils.div(BigDecimalUtils.sub(meigushouyi2, meigushouyi6), meigushouyi6);
                    Double v = Double.parseDouble(div.subtract(div2).setScale(3).toString());
                    if (v <= 0) {
                        return;
                    }
                }

                final BigDecimal div = Double.parseDouble(meigushouyi5)<=0  ?new BigDecimal("0"): BigDecimalUtils.div(BigDecimalUtils.sub(meigushouyi, meigushouyi5), meigushouyi5);
//                final BigDecimal div2 =Double.parseDouble(meigushouyi6)<=0?new BigDecimal("0"): BigDecimalUtils.div(BigDecimalUtils.sub(meigushouyi2, meigushouyi6), meigushouyi6);
//                final BigDecimal div3 =Double.parseDouble(meigushouyi7)<=0?new BigDecimal("0"): BigDecimalUtils.div(BigDecimalUtils.sub(meigushouyi3, meigushouyi7), meigushouyi7);
//                final BigDecimal div4 =Double.parseDouble(meigushouyi8)<=0?new BigDecimal("0"): BigDecimalUtils.div(BigDecimalUtils.sub(meigushouyi4, meigushouyi8), meigushouyi8);

                final Double result = Stream.of(div/*, div2, div3, div4*/).mapToDouble(ee ->getDouble(ee.setScale(3))).average().getAsDouble();
                if(result<=0){
                    return;
                }
                Map<String,Object> map = new LinkedHashMap<>();
                map.put("symbol",symbol);
                map.put("result",result*100);
                mapList.add(map);
            }
        });
        final Map<String, Map> stringMap = mapList.stream().collect(Collectors.toMap(ee -> ee.get("symbol").toString(), Function.identity()));
        final String[] symbols = mapList.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new);
        final List<Map> maps = baseDataService.listStock(symbols);
        final List<Map> collect = maps.stream().filter(e-> getDouble(e.get("shiyinglv_TTM"))>0).peek(e -> {
            final Map map = stringMap.get(e.get("symbol").toString());
            final String s = getStringDiv(e.get("shiyinglv_TTM"), map.get("result"));
            e.put("peg", getString(s,3));
        }).filter(e->getDouble(e.get("peg"))>0).sorted((a, b) -> {
            final Double peg = getDouble(a.get("peg"));
            final Double peg1 = getDouble(b.get("peg"));
            return peg.compareTo(peg1);
        }).collect(Collectors.toList());

        return collect;
    }




}
