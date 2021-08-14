package com.delicacy.common.utils;

import cn.hutool.core.util.ObjectUtil;

import java.util.Arrays;

/**
 * @author yutao
 * @create 2020-05-13 9:01
 **/
public class ObjectUtils {
    private ObjectUtils() {
    }

    public static Boolean isEmpty(Object object) {
        return ObjectUtil.isEmpty(object);
    }

    public static Boolean isAllEmpty(Object... objects) {
        if (isEmpty(objects)) return true;
        return Arrays.stream(objects).allMatch(e -> isEmpty(e));
    }

    public static Boolean isAnyEmpty(Object... objects) {
        if (isEmpty(objects)) return true;
        return Arrays.stream(objects).anyMatch(e -> isEmpty(e));
    }

    //判断是否为基本类型，包括包装类型和非包装类型
    public static Boolean isBasicType(Object object) {
        return ObjectUtil.isBasicType(object);
    }


}
