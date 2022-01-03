package com.delicacy.miniapp.service.service;

import java.util.List;
import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:11
 **/
public interface AnalysisMoneyService extends FinanceService{


    List<Map> list(String... symbols);


}
