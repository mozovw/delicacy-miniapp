package com.delicacy.miniapp.service.runner;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.Arrays;

@Slf4j
public abstract class AbstractRunner implements CommandLineRunner {
    static int threadNum = Runtime.getRuntime().availableProcessors();

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected boolean checkArgs(int len, String s1, String... args) {
        if (args == null || args.length == 0) {
            return false;
        }
        if (!args[0].toLowerCase().trim().equalsIgnoreCase(s1)) {
            return false;
        }
        if (args.length != len) {
            log.error("args size is not" + len);
            return false;
        }
        boolean b = Arrays.stream(args).anyMatch(e -> StringUtils.isEmpty(e));
        if (b) {
            log.error("args hava null");
            return false;
        }
        return true;
    }

    protected void dropCollection(String analysis) {
        if(mongoTemplate.collectionExists(analysis)){
            mongoTemplate.dropCollection(analysis);
        }
    }

    protected String getCommand(Object object) {
        String simpleName = getCapitalInitials(object.getClass().getSimpleName());
        String command = simpleName.substring(0, simpleName.length() - 1);
        String capitalInitials = "-" + getCapitalInitials(command);
        return capitalInitials;
    }

    protected String getCapitalInitials(String string) {
        char[] chars = string.toCharArray();
        StringBuilder s = new StringBuilder();
        for (Character c :
                chars) {
            if (Character.isUpperCase(c)) {
                s.append(c);
            }
        }
        return s.toString();
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
                .setDomain(domain)
                .setSleepTime(5000)
                .setCharset("utf-8")
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
    }

}