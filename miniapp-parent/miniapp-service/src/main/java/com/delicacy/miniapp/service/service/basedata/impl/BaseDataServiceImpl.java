package com.delicacy.miniapp.service.service.basedata.impl;

import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-07-28 15:21
 **/
@Service
@CacheConfig(cacheNames = {"StockService"}, keyGenerator = "wiselyKeyGenerator")
public class BaseDataServiceImpl extends AbstractService implements BaseDataService {



    @Autowired
    private ApplicationContext applicationContext;

    // 获取所有的symbol
    @Cacheable
    @Override
    public List<String> allSymbol() {
        final List<Map> maps = list("xueqiu_astock", new Query());
        return maps.stream().map(e -> getRealSymbol(e.get("symbol").toString())).collect(Collectors.toList());
    }

    @Override
    @Cacheable
    public List<String> allPrefixSymbol() {
        final List<Map> maps = list("xueqiu_astock", new Query());
        return maps.stream().filter(e->!isEmpty(e.get("prefix_symbol"))).map(e -> e.get("prefix_symbol").toString()).distinct().collect(Collectors.toList());
    }

    @Override
    @Cacheable
    public List<Map> listStock(String... symbols) {
        final List<Map> xueqiu_astock = list("xueqiu_astock", symbols);
        final List<Map> xueqiu_astock_desc = list("xueqiu_astock_desc", symbols);
        final Map<Object, Map> objectMapMap = xueqiu_astock_desc.stream().collect(Collectors.toMap(e -> e.get("symbol"), Function.identity()));

        xueqiu_astock.forEach(e -> {
            final Map map = objectMapMap.get(e.get("symbol"));
            if (!isEmpty(map)) {
                e.put("platename", map.get("platename"));
            }
            if (!isEmpty(e.get("52zhouzuidi")) && !isEmpty(e.get("52zhouzuigao")) && !isEmpty(e.get("current"))) {
                String aa = String.valueOf(e.get("52zhouzuidi"));
                String bb = String.valueOf(e.get("52zhouzuigao"));
                String cc = String.valueOf(e.get("current"));
                String s = BigDecimal.valueOf(Double.parseDouble(cc) - Double.parseDouble(aa)).divide(
                        BigDecimal.valueOf(Double.parseDouble(bb) - Double.parseDouble(aa)), 3, RoundingMode.UP
                ).setScale(3, RoundingMode.UP).toString();
                e.put("gaodi", s);
            }
            e.put("symbol", getRealSymbol(e.get("symbol").toString())) ;
        });
        return xueqiu_astock;
    }

    @Override
    @Cacheable
    public List<Map> listReport(String... symbols) {
        return list("xueqiu_astock_report", symbols);
    }

    @Override
    @Cacheable
    public List<Map> listProfitReport(String... symbols) {
        return list("xueqiu_astock_profit_report", symbols);
    }

    @Override
    @Cacheable
    public List<Map> listCashFlowReport(String... symbols) {
        return list("xueqiu_astock_cash_flow_report", symbols);
    }

    @Override
    @Cacheable
    public List<Map> listBalanceReport(String... symbols) {
        return list("xueqiu_astock_balance_report", symbols);
    }

    @Override
    @Cacheable
    public List<Map> listFundRank(String... symbols) {
        return list("aijijin_fund_rank", symbols);
    }


    @Override
    @Cacheable
    public List<Map> listSkHolder(String... symbols) {
        return list("xueqiu_astock_sk_holder_chg", symbols);
    }

    @Override
    @Cacheable
    public List<Map> listTopHolder(String... symbols) {
        return list("xueqiu_astock_top_holders", symbols);
    }


    @Override
    public List<Map> list(String table, String... symbols) {
        Query query = new Query();
        if (isEmpty(symbols)) {
            return new ArrayList<>();
        }
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        return list(table,query);
    }

    @Override
    public List<Map> list(String table, Query query) {
        final List<Map> mapList = mongoTemplate.find(query, Map.class, table);
        mapList.forEach(e->{
            e.remove("_id");
        });
        return mapList;
    }

}
