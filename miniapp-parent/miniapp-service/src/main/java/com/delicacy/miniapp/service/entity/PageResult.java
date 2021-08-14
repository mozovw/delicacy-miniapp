package com.delicacy.miniapp.service.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author yutao.zhang
 * @create 2021-08-04 15:29
 **/
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class PageResult<T>{
    private int pageNum;
    private int pageSize;
    private List<T> list;
    private long total;

}
