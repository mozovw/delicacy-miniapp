package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.AnalysisFundRankPositionService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-07-28 15:21
 **/
@Service
public class AnalysisFundRankPositionServiceImpl extends AbstractService implements AnalysisFundRankPositionService {

    @Autowired
    protected MongoTemplate mongoTemplate;

//    @Override
//    public List<Object> list() {
//        Query query = new Query();
//        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_astock_report");
//        List<Object> symbols = maps.stream().map(e -> e.get("symbol")).collect(Collectors.toList());
//        return symbols;
//    }

    @Override
    public List<Map> list(String... symbols) {
        fund_rank_position("jin1yue");
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("symbol").in(symbols)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_fund_rank_position");
        return maps;
    }

    @Override
    // 工作日下午四点
    public List<List<Map>> list() {
        fund_rank_position("jin1zhou");

        List<String> list = new ArrayList<>();

        get10DaysDatas(list);

        String[] strings = list.toArray(new String[0]);
        List<List<Map>> lists = getLists(strings);
        return lists;
    }

    private void get10DaysDatas(List<String> list) {
        list.add("analysis_fund_rank_position");
        DateTime dateTime = DateTime.now();
        while (list.size() <= 9) {
            String yyyy_mm_dd = DateUtil.format(dateTime, "yyyy_MM_dd");
            String rankposition = "analysis_fund_rank_position_" + yyyy_mm_dd;
            if (mongoTemplate.collectionExists(rankposition)) {
                list.add(rankposition);
            }
            dateTime = DateUtil.offsetDay(dateTime, -1);
        }



    }

    private List<List<Map>> getLists(String... strings) {
        Query query = new Query();
        return Arrays.stream(strings).map(e -> mongoTemplate.find(query, Map.class, e).stream().peek(ee->{
            if (e.equals("analysis_fund_rank_position")){
                String yyyy_mm_dd = DateUtil.format(DateTime.now(), "yyyy_MM_dd");
                ee.put("date",yyyy_mm_dd);
            }else{
                String date = e.replace("analysis_fund_rank_position_", "");
                DateTime yyyy_mm_dd = DateUtil.parse(date, "yyyy_MM_dd");
                DateTime dateTime = DateUtil.offsetDay(yyyy_mm_dd, -1);
                String ymd = DateUtil.format(dateTime, "yyyy_MM_dd");
                ee.put("date", ymd);
            }
        }).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public void fund_rank_position(String leixing) {
        // 删除table
        String analysis_table = "analysis_fund_rank_position";
        batchDropByDate(analysis_table);
        boolean spider = renameCollection("spider", analysis_table);
        if (spider) {
            dropCollection(analysis_table);
        }

        //  近1月赢利最多的基金
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("type").in("gpx", "hhx", "ETF").
                        and(Objects.nonNull(leixing) ? leixing : "jin1yue").ne("")
        ));
        List<Map> list = mongoTemplate.find(query, Map.class, "aijijin_fund_rank");
        Collections.sort(list, Comparator.comparing(e -> Double.valueOf((String) ((Map) e).get(Objects.nonNull(leixing) ? leixing : "jin1yue"))).reversed());

        List<Object> symbol = list.stream().map(e -> e.get("symbol")).limit(1000).collect(Collectors.toList());

        query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("fund_code").in(symbol)
        ));
        List<Map> maps = mongoTemplate.find(query, Map.class, "aijijin_fund_position");

        //  分组排序 根据股票代码分组，占净值比例>4
        Map<Object, List<Map>> collect = maps.stream().filter(e -> {
            try {
                if (e.get("gupiaodaima") == null) {
                    return false;
                }
                Double zhanjingzhibili = percentData(e.get("zhanjingzhibi").toString());
                return zhanjingzhibili > 0;
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).collect(Collectors.groupingBy(e ->
                e.get("gupiaodaima")
        ));
        //  显示结果 股票代码 股票名称 买入总个数
        ArrayList<String> objects = Lists.newArrayList();
        collect.entrySet().stream().forEach(e -> {
            int size = e.getValue().size();
            if (size >= 5) {
                String s = String.format("%s_%s_%s", e.getKey(), e.getValue().get(0).get("gupiaomingcheng"), size);
                objects.add(s);
                LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
                map.put("symbol", e.getKey());
                map.put("name", e.getValue().get(0).get("gupiaomingcheng"));
                map.put("count", String.valueOf(size));
//                addData(map, analysis_table, "symbol", "name", "count");
                addData(map, analysis_table);
            }
        });
        Collections.sort(objects, Comparator.comparing(e -> Integer.valueOf(((String) e).split("_")[2])).reversed());

    }

    private void batchDropByDate(String table) {
        DateTime dateTime = DateUtil.offsetMonth(new Date(), -3);
        DateTime dateTime2 = DateUtil.offsetDay(new Date(), -20);
        do {
            dateTime = DateUtil.offsetDay(dateTime, 1);
            String yyyy_mm_dd = DateUtil.format(dateTime, "yyyy_MM_dd");
            dropCollection(String.format("%s_%s", table, yyyy_mm_dd));
        } while (dateTime.getTime() < dateTime2.getTime());
    }
}
