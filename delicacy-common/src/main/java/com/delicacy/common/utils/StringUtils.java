package com.delicacy.common.utils;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yutao.zhang
 * @create 2021-07-30 10:28
 **/
public class StringUtils {
    private StringUtils() {
    }

    public static String subBefore(String str, String separator) {
        return StrUtil.subBefore(str, separator, false);
    }

    public static String subAfter(String str, String separator) {
        return StrUtil.subAfter(str, separator, true);
    }

    private final static Pattern linePattern = Pattern.compile("_(\\w)");

    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String humpToLine(String str) {
        String lowerCase = str.replaceAll("[A-Z]", "_$0").toLowerCase();
        return lowerCase.substring(1);
    }
}
