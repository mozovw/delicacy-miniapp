package com.delicacy.miniapp.service.service;

import com.delicacy.miniapp.service.entity.PageResult;

import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:16
 **/
public interface FinanceFundSelectionService {

    void runFundSelection();

    PageResult<Map> pageFundSelection(Map params);
}

