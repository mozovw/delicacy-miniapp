package com.delicacy.miniapp.service.service.analysis;

import java.util.List;
import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-07-29 10:11
 **/
public interface AnalysisService {
    List<Map> list(String... symbols);
}