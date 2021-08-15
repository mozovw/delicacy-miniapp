package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.math.MathUtil;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.AnalysisMoneyService;
import com.delicacy.miniapp.service.service.AnalysisStockService;
import com.delicacy.miniapp.service.service.FinanceValuationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:19
 **/
@Slf4j
@Service
public class FinanceValuationServiceImpl extends AbstractService implements FinanceValuationService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    private AnalysisMoneyService analysisMoneyService;

    @Autowired
    private AnalysisStockService analysisStockService;

    final static String FINANCE_VALUATION = "finance_valuation";

    @Override
    public void runValuation() {
        dropCollection(FINANCE_VALUATION);
        List<Map> mapList = analysisMoneyService.list();
        String[] symbols = mapList.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new);

        List<Map> maps3 = analysisStockService.list(symbols);
        if (isEmpty(maps3)) {
            return;
        }
        List<Map> maps = addAllMap(maps3, mapList);

        List<Map> collect = maps.stream().filter(e -> {
            Double jl_current = getDouble(e.get("jl_current"));
            Double current = getDouble(e.get("current"));
            Double yy_current = getDouble(e.get("yy_current"));
            return jl_current > current &&
                    yy_current > current;
        }).peek(e->{
            Double jl_current = getDouble(e.get("jl_current"));
            Double current = getDouble(e.get("current"));
            String s = new BigDecimal(jl_current).divide(new BigDecimal(current), RoundingMode.HALF_DOWN).setScale(1, RoundingMode.HALF_DOWN).toString();
            e.put("gushibi",s);
        }).sorted((a, b) -> {
            Double a_jl_current = getDouble(a.get("gushibi"));
            Double b_jl_current = getDouble(b.get("gushibi"));
            return a_jl_current > b_jl_current ? -1 : 1;
        }).collect(Collectors.toList());


        collect.forEach(e -> {
            addData(e, FINANCE_VALUATION);
        });
    }

    private Double getDouble(Object o) {
        if (o == null){
            return 0.0;
        }
        return Double.parseDouble(o.toString());
    }


    @Override
    public PageResult<Map> pageValuation(Map params) {
        PageResult<Map> mapPageResult = getMapPageResult(params, FINANCE_VALUATION);
        return mapPageResult;
    }


}
