package com.delicacy.miniapp.service.service.finance.impl;

import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.analysis.CashFlowService;
import com.delicacy.miniapp.service.service.analysis.StockService;
import com.delicacy.miniapp.service.service.finance.MoneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-07-28 15:21
 **/
@Service
public class MoneyServiceImpl extends AbstractService implements MoneyService {

    final static String FINANCE_VALUATION = "finance_valuation";
    @Autowired
    private CashFlowService cashFlowService;
    @Autowired
    private StockService analysisStockService;

    private Double getDouble(Object o) {
        if (o == null) {
            return 0.0;
        }
        return Double.parseDouble(o.toString());
    }

    @Override
    public void runTask() {
        dropCollection(FINANCE_VALUATION);
        List<Map> mapList = cashFlowService.list();
        String[] symbols = mapList.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new);

        List<Map> maps3 = analysisStockService.list(symbols);
        if (isEmpty(maps3)) {
            return;
        }
        List<Map> maps = addAllMap(maps3, mapList);

        List<Map> collect = maps.stream().filter(e -> {
            Double xianjinliu_current = getDouble(e.get("xianjinliu_current"));
            Double current = getDouble(e.get("current"));

            return xianjinliu_current > current;
        }).peek(e -> {
            Double xianjinliu_current = getDouble(e.get("xianjinliu_current"));
            Double current = getDouble(e.get("current"));
            String s = new BigDecimal(xianjinliu_current).divide(new BigDecimal(current), RoundingMode.HALF_DOWN).setScale(1, RoundingMode.HALF_DOWN).toString();
            e.put("gushibi", s);
        }).sorted((a, b) -> {
            Double a_xianjinliu_current = getDouble(a.get("gushibi"));
            Double b_xianjinliu_current = getDouble(b.get("gushibi"));
            return a_xianjinliu_current > b_xianjinliu_current ? -1 : 1;
        }).collect(Collectors.toList());


        collect.forEach(e -> {
            addData(e, FINANCE_VALUATION);
        });
    }

    @Override
    public PageResult<Map> page(Map params) {
        PageResult<Map> mapPageResult = getMapPageResult(params, FINANCE_VALUATION);
        return mapPageResult;
    }
}
