package com.delicacy.miniapp.service.service.basedata;

import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:11
 **/
public interface BaseDataService {

    List<String> allSymbol();
    List<String> allPrefixSymbol();

    List<Map> listStock(String... symbols);


    List<Map> listReport(String... symbols);


    List<Map> listProfitReport(String... symbols);


    List<Map> listCashFlowReport(String... symbols);


    List<Map> listBalanceReport(String... symbols);


    List<Map> listFundRank(String... symbols);


    List<Map> listSkHolder(String... symbols);


    List<Map> listTopHolder(String... symbols);

    List<Map> list(String table, String... symbols);

    List<Map> list(String table, Query query);

}
