package com.delicacy.miniapp.service.service.finance;

import com.delicacy.miniapp.service.entity.PageResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-08-04 14:16
 **/
public interface FinanceService {

    void runTask();

    PageResult<Map> page(Map params);
}

