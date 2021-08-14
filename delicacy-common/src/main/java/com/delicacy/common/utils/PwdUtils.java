package com.delicacy.common.utils;

import cn.hutool.core.util.RandomUtil;

/**
 * @author yutao
 * @create 2020-05-19 16:51
 **/
public class PwdUtils {
    private PwdUtils() {
    }

    public static String getSixRandString() {
        return RandomUtil.randomString(6);
    }
}
