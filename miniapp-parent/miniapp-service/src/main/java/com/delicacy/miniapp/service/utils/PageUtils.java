package com.delicacy.miniapp.service.utils;

import com.delicacy.common.utils.ObjectUtils;
import com.delicacy.miniapp.service.entity.PageResult;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PageUtils {

    public  <T> PageResult<T> pageInfo(List<T> list, Integer pageNo, Integer pageSize) {
        int startRow = (pageNo - 1) * pageSize;
        int endRow = pageNo * pageSize;
        int size = list == null ? 0 : list.size();
        List<T> subList = new ArrayList();
        if (!ObjectUtils.isEmpty(list)) {
            if (endRow >= size && startRow <= size) {
                subList = list.subList(startRow, size);
            }
            if (endRow < size) {
                subList = list.subList(startRow, endRow);
            }
        }
        PageResult<T> result = new PageResult<T>();
        result.setList(subList);
        result.setPageNum(pageNo);
        result.setPageSize(pageSize);
        result.setTotal(size);
        return result;
    }
}