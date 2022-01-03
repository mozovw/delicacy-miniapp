package com.delicacy.miniapp.service.service.finance.impl;

import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.*;
import com.delicacy.miniapp.service.service.analysis.FundRankPositionService;
import com.delicacy.miniapp.service.service.analysis.StockReportService;
import com.delicacy.miniapp.service.service.analysis.StockService;
import com.delicacy.miniapp.service.service.finance.ComprehensiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:19
 **/
@Slf4j
@Service
public class ComprehensiveServiceImpl extends AbstractService implements ComprehensiveService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    private FundRankPositionService fundRankPositionService;
    @Autowired
    private StockService stockService;
    @Autowired
    private StockReportService stockReportService;
    final static String FINANCE_COMPREHENSIVE = "finance_comprehensive";

    @Override
    public void runTask() {

        dropCollection(FINANCE_COMPREHENSIVE);
        List<Map> maps1 = stockReportService.list();
        if (isEmpty(maps1)) {
            return;
        }
        String[] symbols1 = maps1.stream().map(e -> e.get("symbol").toString()).toArray(String[]::new);
        List<Map> maps2 = fundRankPositionService.list(symbols1);
        if (isEmpty(maps2)) {
            return;
        }
        List<Map> maps = addAllMap(maps1, maps2);

        String[] symbols2 = maps2.stream().filter(e -> Integer.parseInt(e.get("count").toString()) >= 10
        ).map(e -> e.get("symbol").toString()).toArray(String[]::new);

        List<Map> maps3 = stockService.listByFilter(symbols2);
        if (isEmpty(maps3)) {
            return;
        }
        List<Map> maps4 = maps3.stream().map(e -> {
            Object symbol = e.get("symbol");
            Map map = maps2.stream().filter(ee -> symbol.equals(ee.get("symbol"))).findFirst().get();
            e.putAll(map);
            return e;
        }).sorted(Comparator.comparing(e -> -Integer.parseInt(e.get("count").toString())))
                .collect(Collectors.toList());
        maps = addAllMap(maps4, maps);

        maps.forEach(e -> {
            addData(e, FINANCE_COMPREHENSIVE);
        });

    }


    @Override
    public PageResult<Map> page(Map params) {
        return getMapPageResult(params,FINANCE_COMPREHENSIVE);
    }
}
