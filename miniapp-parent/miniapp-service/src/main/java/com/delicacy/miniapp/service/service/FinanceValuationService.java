package com.delicacy.miniapp.service.service;

import com.delicacy.miniapp.service.entity.PageResult;

import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:16
 **/
public interface FinanceValuationService {

    void runValuation();

    PageResult<Map> pageValuation(Map params);
}

