package com.delicacy.common.utils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;

public class DateUtils {
    private DateUtils() {
    }

    public String formatYMD(Date startDate) {
        return DateUtil.format(startDate, "yyyy-MM-dd");
    }

    public Date parseYMD(String startDate) {
        return DateUtil.parse(startDate, "yyyy-MM-dd");
    }

    public long betweenDays(Date startDate, Date finishDate) {
        return DateUtil.between(startDate, finishDate, DateUnit.DAY, true);
    }

    public long betweenDaysNoABS(Date startDate, Date finishDate) {
        return DateUtil.between(startDate, finishDate, DateUnit.DAY, false);
    }

    public long betweenSeconds(Date startDate, Date finishDate) {
        return DateUtil.between(startDate, finishDate, DateUnit.SECOND, true);
    }

    public long betweenMinutes(Date startDate, Date finishDate) {
        return DateUtil.between(startDate, finishDate, DateUnit.MINUTE, true);
    }

    public long betweenHours(Date startDate, Date finishDate) {
        return DateUtil.between(startDate, finishDate, DateUnit.HOUR, true);
    }

    public DateTime offsetDay(Date startDate, Integer a) {
        return DateUtil.offsetDay(startDate, a);
    }

    public DateTime offsetYear(Date startDate, Integer a) {
        return DateUtil.offset(startDate, DateField.YEAR, a);
    }


    public DateTime beginOfYear(Date date) {
        return DateUtil.beginOfYear(date);
    }
}
