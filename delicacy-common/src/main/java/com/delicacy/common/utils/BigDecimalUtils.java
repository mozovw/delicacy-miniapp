package com.delicacy.common.utils;

import cn.hutool.core.util.ObjectUtil;

import java.math.BigDecimal;

public class BigDecimalUtils {

    private BigDecimalUtils() {
    }

    public static BigDecimal add(Object v1, Object v2) {// v1 + v2
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.add(b2);
    }

    public static BigDecimal sub(Object v1, Object v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.subtract(b2);
    }

    public static BigDecimal mul(Object v1, Object v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.multiply(b2);
    }

    public static BigDecimal div(Object v1, Object v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        // 2 = 保留小数点后两位   ROUND_HALF_UP = 四舍五入  
        return b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);// 应对除不尽的情况  
    }

    private static String convert(Object object) {
        Boolean basicType = ObjectUtil.isBasicType(object);
        if (basicType) return String.valueOf(object);
        throw new RuntimeException("数据异常");
    }

    private static BigDecimal createBigDecimal(Object o) {
        if (o == null) throw new IllegalArgumentException();
        return o instanceof BigDecimal ? (BigDecimal) o : new BigDecimal(convert(o));
    }

}