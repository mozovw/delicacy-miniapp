package com.delicacy.miniapp.rest;


import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.AnalysisFundRankPositionService;
import com.delicacy.miniapp.service.service.FinanceComprehensiveService;
import com.delicacy.miniapp.service.service.FinanceFundSelectionService;
import com.delicacy.miniapp.service.service.FinanceValuationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@Validated
@RestController
@RequestMapping("/financialdata")
@Api(value = "/financialdata", tags = "金融数据")
public class FinancialDataRest {

    @Autowired
    private AnalysisFundRankPositionService analysisFundRankPositionService;
    @Autowired
    private FinanceFundSelectionService financeFundSelectionService;

    @Autowired
    private FinanceComprehensiveService financeComprehensiveService;

    @Autowired
    private FinanceValuationService financeValuationService;

    @PostMapping("pageComprehensive")
    @ApiOperation(value = "综合分析")
    public PageResult<Map> pageComprehensive(@RequestBody Map map) {
        return financeComprehensiveService.pageComprehensive(map);
    }

    @PostMapping("pageFundSelection")
    @ApiOperation(value = "基金选择")
    public PageResult<Map> pageFundSelection(@RequestBody Map map) {
        return financeFundSelectionService.pageFundSelection(map);
    }

    @PostMapping("pageValuation")
    @ApiOperation(value = "估值分析")
    public PageResult<Map> pageValuation(@RequestBody Map map) {
        return financeValuationService.pageValuation(map);
    }


    @GetMapping("list")
    @ApiOperation(value = "列表")
    public List<List<Map>> list() {
        return analysisFundRankPositionService.list();
    }

}
