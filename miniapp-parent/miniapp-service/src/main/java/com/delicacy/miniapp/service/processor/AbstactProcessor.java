package com.delicacy.miniapp.service.processor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.miniapp.service.driver.WebDriverPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.assertj.core.util.Lists;
import org.openqa.selenium.WebDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yutao
 * @create 2019-11-20 15:38
 **/
@Setter
@Getter
public abstract class AbstactProcessor implements PageProcessor {

    static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    private static Set<String> stringSet = new ConcurrentSkipListSet<>();

    static {
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    ObjectMapper objectMapper = new ObjectMapper();
    private Site site;
    private volatile WebDriverPool webDriverPool;

    {
        String path = this.getClass().getResource("/chromedriver.exe").getPath();
        System.getProperties().setProperty("webdriver.chrome.driver", path);
    }

    protected void processPage(Page page, Function<Object, List<String>> function) {
        String rawText = page.getRawText();
        JSONObject jsonObject = JSON.parseObject(rawText);
        if (jsonObject == null) {
            return;
        }
        Object data = jsonObject.get("data");
        if (data == null) {
            return;
        }
        rawText = data.toString();
        jsonObject = JSON.parseObject(rawText);
        rawText = jsonObject.get("list").toString();
        JSONArray jsonArray = JSON.parseArray(rawText);
        if (jsonArray == null || jsonArray.size() == 0) {
            return;
        }

        List<List<String>> collect = jsonArray.stream().map(e -> {
            JSONObject e1 = (JSONObject) e;
            String symbol = e1.get("symbol").toString();
            return function.apply(symbol);
        }).collect(Collectors.toList());
        collect.forEach(page::addTargetRequests);
        //todo update flag
        Map<String, List<String>> stringListMap = HttpUtil.decodeParams(page.getUrl().toString(), "utf-8");
        long longPage = Long.parseLong(stringListMap.get("page").get(0));
//        long sum = longPage * Long.parseLong(stringListMap.get("size").get(0));
//        long count = Long.parseLong(jsonObject.get("count").toString());
        //todo update page
        //todo get newurl
        stringListMap.put("page", Lists.newArrayList(String.valueOf(longPage + 1)));
        String params = HttpUtil.toParams(stringListMap);
        String string = page.getUrl().toString();
        String newUrl = string.substring(0, string.indexOf("?") + 1) + params;
        page.addTargetRequest(newUrl);
    }

    @SneakyThrows
    protected String json(Object o) {
        return objectMapper.writeValueAsString(o);
    }

    @SneakyThrows
    protected <T> T json2Obj(String o, Class<T> clazz) {
        return objectMapper.readValue(o, clazz);
    }

    protected boolean isEmpty(Object o) {
        return ObjectUtil.isEmpty(o);
    }

    @Override
    public Site getSite() {
        return site;
    }


    /**
     * 是否存在中文
     *
     * @param inputString
     * @return
     */
    public boolean hasPingYin(String inputString) {
        if (inputString != null && inputString.length() > 0
                && !"null".equals(inputString)) {
            char[] input = inputString.trim().toCharArray();
            boolean flag = false;
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    flag = true;
                }
            }
            return flag;
        }
        return false;
    }

    protected String trim(String str) {
        str = str.replace((char) 160, ' ');
        return str.trim();
    }

    /**
     * 中文转成拼音
     *
     * @param inputString
     * @param only        只有拼音吗
     * @return
     */
    protected String getPingYin(String inputString, boolean only) {
        String output = "";
        if (inputString != null && inputString.length() > 0
                && !"null".equals(inputString)) {
            char[] input = inputString.trim().toCharArray();
            try {
                for (int i = 0; i < input.length; i++) {
                    if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                        output += temp[0];
                    } else if (!only) {
                        output += Character.toString(input[i]);
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    protected String getPinyinPlus(String inputString) {
        String pingYin = getPingYin(inputString, false);
        if (pingYin.contains("（")) {
            pingYin = pingYin.replace("（", "_").substring(0, pingYin.length() - 1);
        }
        if (pingYin.contains("(")) {
            pingYin = pingYin.replace("(", "_").substring(0, pingYin.length() - 1);
        }
        return pingYin;
    }

    /**
     * 获取WebDriver
     *
     * @return
     */
    protected WebDriver getWebDriver() {
        if (webDriverPool == null)
            synchronized (WebDriverPool.class) {
                if (webDriverPool == null) {
                    webDriverPool = new WebDriverPool(Runtime.getRuntime().availableProcessors());
                }
            }
        try {
            return webDriverPool.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 将webDriverPool返回到池子
     *
     * @param webDriver
     */
    protected void returnToPool(WebDriver webDriver) {
        if (webDriverPool != null)
            synchronized (WebDriverPool.class) {
                if (webDriverPool != null) {
                    webDriverPool.returnToPool(webDriver);
                }
            }
    }

    /**
     * 过滤文本
     *
     * @param replace
     * @return
     */
    protected boolean filerContent(String replace) {
        if (!stringSet.contains(replace)) {
            stringSet.add(replace);
        } else {
            stringSet.remove(replace);
            return true;
        }
        return false;
    }

    protected static class PageProcessor {
        Page page;
        JSONObject jsonObject;
        Integer size = 0;
        JSONArray jsonArray;
        Map<Integer, JSONObject> jsonObjectMap = Maps.newLinkedHashMap();

        LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();

        public PageProcessor(Page page, JSONObject jsonObject) {
            this.page = page;
            this.jsonObject = jsonObject;
        }

        public PageProcessor(Page page, JSONArray jsonArray) {
            this.page = page;
            this.jsonArray = jsonArray;
            this.size = jsonArray.size();
            for (int i = 0; i < size; i++) {
                LinkedHashMap<String, String> map = new LinkedHashMap();
                mapMain.put(i, map);
                jsonObjectMap.put(i, jsonArray.getJSONObject(i));
            }
        }

        public void putmap(Integer index, String a, String b) {
            this.mapMain.get(index).put(a, b);
        }

        public LinkedHashMap<String, String> getmap(Integer index) {
            return this.mapMain.get(index);
        }

        public void transfer(Integer index, String a, String b) {
            transfer(this.mapMain.get(index), jsonObjectMap.get(index), a, b);
        }

        public void process() {
            page.putField("map", mapMain);
        }

        private void transfer(Map page, Object jsonObject, String a, String b) {
            if (b == null) {
                page.put(a, null);
                return;
            }
            Object obj = null;

            if (jsonObject instanceof JSONObject) {
                obj = ((JSONObject) jsonObject).get(b);
            } else {
                if (jsonObject != null) {
                    obj = jsonObject;
                }
            }

            if (obj == null) {
                page.put(a, null);
                return;
            }

            String string = null;
            if (obj instanceof String) {
                string = String.valueOf(obj);
            } else if (obj instanceof BigDecimal) {
                string = ((BigDecimal) obj).setScale(3, RoundingMode.HALF_UP).toString();
            } else if (obj instanceof Long) {
                string = ((Long) obj).toString();
            } else if (obj instanceof Integer) {
                string = ((Integer) obj).toString();
            } else if (obj instanceof JSONArray) {
                transfer(page, ((JSONArray) obj).get(0), a, b);
                return;
            }
            page.put(a, string);
        }

        private void transfer(Page page, JSONObject jsonObject, String a, String b) {
            if (b == null) {
                page.putField(a, null);
                return;
            }
            Object obj = jsonObject.get(b);
            if (obj == null) {
                page.putField(a, null);
                return;
            }
            String string = null;
            if (obj instanceof String) {
                string = String.valueOf(obj);
            } else if (obj instanceof BigDecimal) {
                string = ((BigDecimal) obj).setScale(3, RoundingMode.HALF_UP).toString();
            } else if (obj instanceof Long) {
                string = ((Long) obj).toString();
            } else if (obj instanceof Integer) {
                string = ((Integer) obj).toString();
            }
            page.putField(a, string);
        }

        public void transfer(String a, String b) {
            if (jsonObject != null && page != null) {
                transfer(page, jsonObject, a, b);
            }
        }
    }


}
