package com.delicacy.miniapp.service.service;

import cn.hutool.core.date.DateUtil;
import com.delicacy.common.utils.ObjectUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.pipeline.Map2MongoPipeline;
import com.delicacy.miniapp.service.pipeline.MongoPipeline;
import com.delicacy.miniapp.service.utils.PageUtils;
import com.google.common.collect.Maps;
import com.mongodb.MongoNamespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:16
 **/
public abstract class AbstractService {
    protected static int threadNum = Runtime.getRuntime().availableProcessors();

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected PageResult<Map> getMapPageResult(Map params, String table) {
        String pageNum = params.get("pageNum").toString();
        String pageSize = params.get("pageSize").toString();
        Object o = params.get("query");
        Query query = new Query();
        if (!isEmpty(o)) {
            String text = o.toString();
            Pattern pattern = Pattern.compile("^.*" + text + ".*$", Pattern.CASE_INSENSITIVE);
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("symbol").in(text),
                    Criteria.where("name").regex(pattern),
                    Criteria.where("report_date").regex(pattern)
                    )
            );
        }
        List<Map> maps = mongoTemplate.find(query, Map.class, table);
        PageResult<Map> mapPageResult = PageUtils.pageInfo(maps, Integer.parseInt(pageNum), Integer.parseInt(pageSize));
        return mapPageResult;
    }

    protected boolean isEmpty(Object s) {
        return ObjectUtils.isEmpty(s);
    }

    protected void dropCollection(String analysis) {
        if (mongoTemplate.collectionExists(analysis)) {
            mongoTemplate.dropCollection(analysis);
        }
    }

    protected void runSpiderForMap2(Request request, PageProcessor pageProcessor, String collectName, String... params) {
        Pipeline pipeline = new Map2MongoPipeline(mongoTemplate, collectName, params);
        Spider.create(pageProcessor)
                .thread(threadNum)
                .addRequest(request)
                .addPipeline(new ConsolePipeline())
                .addPipeline(pipeline)
                .runAsync();

    }

    protected void runSpider(Request request, PageProcessor pageProcessor, String collectName, String... params) {
        Pipeline pipeline = new MongoPipeline(mongoTemplate, collectName, params);
        Spider.create(pageProcessor)
                .thread(threadNum)
                .addRequest(request)
                .addPipeline(new ConsolePipeline())
                .addPipeline(pipeline)
                .runAsync();
    }


    protected Request getRequest(String url) {
        Request request = new Request();
        request.setUrl(url);
        request.setMethod(HttpConstant.Method.GET);
        request.setCharset("utf-8");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
        return request;
    }

    protected Site getSite(String domain) {
        return Site
                .me()
                .setRetryTimes(2)
                .setRetrySleepTime(5000)
                .setTimeOut(10000)
                .setDomain(domain)
                .setSleepTime(5000)
                .setCharset("utf-8")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
    }

    protected void addData(Map e, String table) {
        mongoTemplate.insert(e, table);
    }

    protected void addData(Map e, String table, String... keys) {
        LinkedHashMap<Object, Object> objectObjectLinkedHashMap = Maps.newLinkedHashMap();
        for (String key : keys) {
            if (key.contains("=")) {
                String[] split = key.split("=");
                objectObjectLinkedHashMap.put(split[0], split[1]);
                continue;
            }
            objectObjectLinkedHashMap.put(key, e.get(key));
        }
        mongoTemplate.insert(objectObjectLinkedHashMap, table);
    }


    protected boolean renameCollection(String dbName, String analysis) {
        if (mongoTemplate.collectionExists(analysis)) {
            String yyyy_mm_dd = DateUtil.format(new Date(), "yyyy_MM_dd");
            String fullName = analysis + "_" + yyyy_mm_dd;
            if (mongoTemplate.collectionExists(fullName)) return false;
            mongoTemplate.getCollection(analysis).renameCollection(new MongoNamespace(dbName, fullName));
            Query query = new Query();
            List<Map> maps = mongoTemplate.find(query, Map.class, fullName);
            mongoTemplate.insert(maps, analysis);
            return true;
        }
        return false;
    }


    protected void todo(String collectionName, Consumer<List<Map>> consumer) {
        todo(null, collectionName, consumer);
    }

    protected void todo(String orderName, String collectionName, Consumer<List<Map>> consumer) {
        Query query = new Query();
        long count = mongoTemplate.count(query, Map.class, collectionName);
        for (int i = 0; i < count; i++) {
            if (i % 10 != 0) continue;
            if (!ObjectUtils.isEmpty(orderName)) {
                Sort datetime = Sort.by(Sort.Direction.DESC, orderName);
                query = query.with(datetime);
            }
            List<Map> maps = mongoTemplate.find(query.skip(i).limit(10), Map.class, collectionName);
            consumer.accept(maps);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    protected Double percentData(String string) {
        if (string.equals("--")) throw new IllegalArgumentException("exists '--'");
        double value;
        if (string.contains("万%")) {
            value = Double.parseDouble(string.replace("万%", ""));

        } else if (string.contains("%")) {
            value = Double.parseDouble(string.replace("%", ""));
        } else {
            value = Double.parseDouble(string);
        }
        return value;
    }

    protected Double dayData(String string) {
        if (ObjectUtils.isEmpty(string)) return 0.0;
        double value;
        if (string.contains("万天")) {
            value = Double.parseDouble(string.replace("万天", "")) * 10000;
        } else if (string.contains("天")) {
            value = Double.parseDouble(string.replace("天", ""));
        } else {
            value = Double.parseDouble(string);
        }
        return value;
    }

    protected Double numData(String string) {
        if (ObjectUtils.isEmpty(string)) return 0.0;
        double value;
        if (string.contains("万次")) {
            value = Double.parseDouble(string.replace("万次", ""));
        } else if (string.contains("次")) {
            value = Double.parseDouble(string.replace("次", ""));
        } else {
            value = Double.parseDouble(string);
        }
        return value;
    }

    protected Double moneyData(String string) {
        if (string.equals("--")) throw new IllegalArgumentException("exists '--'");
        double value;
        if (string.contains("万亿")) {
            value = Double.parseDouble(string.replace("万亿", "")) * 1000000000000L;
        } else if (string.contains("亿")) {
            value = Double.parseDouble(string.replace("亿", "")) * 100000000L;
        } else if (string.contains("万")) {
            value = Double.parseDouble(string.replace("万", "")) * 10000L;
        } else if (string.contains("元")) {
            value = Double.parseDouble(string.replace("元", "")) * 1L;
        } else {
            value = Double.parseDouble(string);
        }
        return value;
    }

    protected List<Map> addAllMap(List<Map> maps1, List<Map> maps2) {
        List<Map> a = maps1.size() > maps2.size() ? maps1 : maps2;
        List<Map> b = maps1.size() > maps2.size() ? maps2 : maps1;
        List<Map> collect = b.stream().map(e -> {
            Map map = new LinkedHashMap();
            String symbol = e.get("symbol").toString();
            Optional<Map> optionalMap = a.stream().filter(ee -> ee.get("symbol").toString().equals(symbol)).findFirst();
            if (optionalMap.isPresent()) {
                map.putAll(optionalMap.get());
            }
            map.putAll(e);
            return map;
        }).collect(Collectors.toList());
        return collect;
    }

}
