package com.delicacy.miniapp.service.processor;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.delicacy.miniapp.service.driver.WebDriverPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.openqa.selenium.WebDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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

        public PageProcessor(Page page, JSONObject jsonObject) {
            this.page = page;
            this.jsonObject = jsonObject;
        }

        private void transfer(Page page,JSONObject jsonObject ,String a, String b) {
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
            if (jsonObject!=null&&page!=null){
                transfer(page,jsonObject,a,b);
            }
        }
    }


}
