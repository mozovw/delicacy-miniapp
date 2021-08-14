package com.delicacy.common.utils;

import cn.hutool.core.util.ArrayUtil;

/**
 * @author yutao
 * @create 2020-05-15 23:39
 **/
public class ArrayUtils {
    private ArrayUtils() {
    }

    public static <T> T[] append(T[] buffer, T... newElements) {
        return ArrayUtil.append(buffer, newElements);
    }

}
