package com.delicacy.miniapp.service.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.common.utils.ObjectUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.pipeline.Map2MongoPipeline;
import com.delicacy.miniapp.service.pipeline.MongoPipeline;
import com.delicacy.miniapp.service.service.basedata.BaseDataService;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Autowired
    protected BaseDataService baseDataService;

    protected static int threadNum = Runtime.getRuntime().availableProcessors();

    protected String getString(Object o,Integer... n) {
        if (isEmpty(o)) {
            return "0";
        }
        Integer[] num={1};
        if (!isEmpty(n)){
            num[0] = n[0];
        }
        if (o instanceof BigDecimal){
            return ((BigDecimal) o).setScale(num[0], RoundingMode.HALF_DOWN).toString();
        }else{
            return new BigDecimal(o.toString()).setScale(num[0], RoundingMode.HALF_DOWN).toString();
        }
    }

    protected String getStringMul(Object o, Object o1,Integer... n) {
        if (isEmpty(o1) || isEmpty(o)) {
            return "0";
        }
        Integer[] num={1};
        if (!isEmpty(n)){
            num[0] = n[0];
        }
        return new BigDecimal(o.toString()).multiply(new BigDecimal(o1.toString())).setScale(num[0], RoundingMode.HALF_DOWN).toString();
    }

    protected String getStringDiv(Object o, Object o1,Integer... n) {
        if (isEmpty(o1) || isEmpty(o)) {
            return "0";
        }
        Integer[] num={1};
        if (!isEmpty(n)){
            num[0] = n[0];
        }
        return new BigDecimal(o.toString()).divide(new BigDecimal(o1.toString()), num[0], BigDecimal.ROUND_HALF_UP).setScale(num[0], RoundingMode.HALF_DOWN).toString();
    }

    protected Double getDouble(Object o) {
        if (o == null) {
            return 0.0;
        }
        return Double.parseDouble(o.toString());
    }
    protected void initData(final String table,List<Map> list){
        dropCollection(table);
        list.forEach(e -> addData(e, table));
    }

    protected Long getLastTimestamp(String date) {
        String year = date.substring(0, 4);
        String report = date.substring(4);
        String ymd = year;
        switch (report) {
            case "一季报":
                ymd += "-03-31";
                break;
            case "中报":
                ymd += "-06-30";
                break;
            case "三季报":
                ymd += "-09-30";
                break;
            case "年报":
                ymd += "-12-31";
                break;
            default:
                throw new RuntimeException();
        }
        DateTime parse = DateUtil.parse(ymd, "yyyy-MM-dd");
        return parse.getTime();
    }

    protected List<Long> getLast4stampList() {
        List<Long> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            DateTime offset = DateUtil.offset(DateTime.now(), DateField.MONTH, -i);
            String format = DateUtil.format(offset, "MM");
            int month = Integer.parseInt(format);
            if (month==12||month==3||month==6||month==9){
                DateTime dateTime = DateUtil.endOfMonth(offset);
                Long time = DateUtil.parseDateTime(DateUtil.format(dateTime, "yyyy-MM-dd") + " 00:00:00").getTime();
                list.add( time);
            }
        }
        return list;
    }




    protected List<String> getYYYYList(DateTime offset) {
        List<String> yyyyList = new ArrayList<>();
        String yyyy = DateUtil.format(offset, "yyyy");
        yyyyList.add(yyyy + "年报");
        yyyyList.add(yyyy + "三季报");
        yyyyList.add(yyyy + "中报");
        yyyyList.add(yyyy + "一季报");
        return yyyyList;
    }

    protected List<Date> getLastReportDateList(){
        List<Date> list = new ArrayList<>();
        String format = DateUtil.format(DateTime.now(), "MM");
        String format_year = DateUtil.format(DateTime.now(), "yyyy");
        DateTime offset = DateUtil.offset(DateTime.now(), DateField.YEAR, -1);
        String format_year_last = DateUtil.format(offset, "yyyy");
        Integer month = Integer.valueOf(format);
        switch (month){
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: {
                list.add(DateUtil.parseDate(format_year + "-03-31"));
                list.add(DateUtil.parseDate(format_year_last + "-12-31"));
            } break;
            case 7:
            case 8:
            case 9:{
                list.add(DateUtil.parseDate(format_year + "-06-30"));
            } break;
            case 10:
            case 11:
            case 12: list.add(DateUtil.parseDate(format_year + "-09-30"));break;
        }

        return list;
    }

    private List<String> getRemoveReportList(Integer num) {
        DateTime offset = DateUtil.offset(DateTime.now(), DateField.YEAR, num);
        String format = DateUtil.format(offset, "yyyy");
        int year = Integer.parseInt(format);
        List<String> list = new ArrayList<>();
        for (int i = year; i > year-10; i--) {
            list.add( i + "年报");
            list.add( i + "三季报");
            list.add( i + "中报");
            list.add( i + "一季报");
        }
        return list;
    }

    protected void clearBeforeNumYear(String table,Integer num) {
        Query query = new Query();
        String[] values = getRemoveReportList(num).toArray(new String[0]);
        query.addCriteria(
                Criteria.where("report_date").in(values)
        );
        mongoTemplate.remove(query,table);
    }

    protected void clearBefore4Year(String table) {
        Query query = new Query();
        String[] values = getRemoveReportList(-4).toArray(new String[0]);
        query.addCriteria(
                Criteria.where("report_date").in(values)
        );
        mongoTemplate.remove(query,table);
    }

    protected List<String> getReportList() {
        DateTime now = DateTime.now();
        String format = DateUtil.format(now, "yyyy");
        int year = Integer.parseInt(format);

        List<String> list = new ArrayList<>();
        for (int i = year; i > 0; i--) {
            String s = i + "年报";
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "年报");
                break;
            }

            s = i + "三季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "三季报");
                break;
            }

            s = i + "中报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "中报");
                break;
            }

            s = i + "一季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (mongoTemplate.exists(query,"xueqiu_astock_report")) {
                list = Arrays.asList(s, (i - 1) + "一季报");
                break;
            }
        }
        return list;

    }

    protected List<String> getReportList(String collection,Integer num) {
       return getReportList(collection,num,false);
    }

    protected List<String> getReportList(String collection,Integer num,Boolean isgetLast) {
        DateTime offset = DateUtil.offset(DateTime.now(), DateField.YEAR, num);
        String format1 = DateUtil.format(offset, "yyyy");
        int year1 = Integer.parseInt(format1);

        DateTime now = DateTime.now();
        String format = DateUtil.format(now, "yyyy");
        int year = Integer.parseInt(format);

        List<String> list = new ArrayList<>();
        for (int i = year; i > year1; i--) {
            String s = i + "年报";
            Query query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (isgetLast&&mongoTemplate.exists(query, collection)) {
                list = Arrays.asList(s, (i + 1) + "一季报",i  + "三季报");
                break;
            }else {
                list.add(s);
            }

            s = i + "三季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (isgetLast&&mongoTemplate.exists(query, collection)) {
                list = Arrays.asList(s, i + "年报",i + "中报");
                break;
            }else {
                list.add(s);
            }

            s = i + "中报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (isgetLast&&mongoTemplate.exists(query, collection)) {
                list = Arrays.asList(s, i  + "三季报",i+"一季报");
                break;
            }else {
                list.add(s);
            }

            s = i + "一季报";
            query = new Query();
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("report_date").in(s)
            ));
            if (isgetLast&&mongoTemplate.exists(query, collection)) {
                list = Arrays.asList(s, (i - 1) + "年报",i+"中报");
                break;
            }else {
                list.add(s);
            }
        }
        return list;

    }

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
                .run();

    }

    protected void runSpider(Request request, PageProcessor pageProcessor, String collectName, String... params) {
        Pipeline pipeline = new MongoPipeline(mongoTemplate, collectName, params);
        Spider.create(pageProcessor)
                .thread(threadNum)
                .addRequest(request)
                .addPipeline(new ConsolePipeline())
                .addPipeline(pipeline)
                .run();
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
            if (mongoTemplate.collectionExists(fullName)){
                return false;
            }
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
            if (i % 10 != 0) {
                continue;
            }
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
        if ("--".equals(string)) {
            throw new IllegalArgumentException("exists '--'");
        }
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
        if (ObjectUtils.isEmpty(string)){
            return 0.0;
        }
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
        if (ObjectUtils.isEmpty(string)) {
            return 0.0;
        }
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
        if ("--".equals(string)) {
            throw new IllegalArgumentException("exists '--'");
        }
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

    protected Map addAllMap(Map maps1, Map maps2) {
        final List<Map> maps = addAllMap(Arrays.asList(maps1), Arrays.asList(maps2));
        if (isEmpty(maps)){
            return new LinkedHashMap();
        }
        return maps.get(0);
    }

    protected List<Map> addAllMap(List<Map> maps1, List<Map> maps2) {
        List<Map> a = maps1.size() > maps2.size() ? maps1 : maps2;
        List<Map> b = maps1.size() > maps2.size() ? maps2 : maps1;
        List<Map> collect = b.stream().map(e -> {
            Map map = new LinkedHashMap();
            String symbol = getRealSymbol(e.get("symbol").toString());
            Optional<Map> optionalMap = a.stream().filter(ee -> {
                final String aa =getRealSymbol( ee.get("symbol").toString());
                return aa.equals(symbol);
            }).findFirst();
            if (optionalMap.isPresent()) {
                map.putAll(optionalMap.get());
            }
            map.putAll(e);
            return map;
        }).collect(Collectors.toList());
        return collect;
    }

    protected String getRealSymbol(String val){
        if (isEmpty(val)){
            return "";
        }
       return val.replace("SH", "").replace("SZ", "");
    }

}
