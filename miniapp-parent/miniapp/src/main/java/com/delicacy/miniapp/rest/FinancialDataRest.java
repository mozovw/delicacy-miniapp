package com.delicacy.miniapp.rest;


import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.analysis.FundRankPositionService;
import com.delicacy.miniapp.service.service.finance.ComprehensiveService;
import com.delicacy.miniapp.service.service.finance.FundSelectionService;
import com.delicacy.miniapp.service.service.finance.MoneyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@Validated
@RestController
@RequestMapping("/financialdata")
@Api(value = "/financialdata", tags = "金融数据")
public class FinancialDataRest {

    @Autowired
    private FundSelectionService financeFundSelectionService;

    @Autowired
    private ComprehensiveService financeComprehensiveService;

    @Autowired
    private MoneyService moneyService;

    @PostMapping("pageComprehensive")
    @ApiOperation(value = "综合分析")
    public PageResult<Map> pageComprehensive(@RequestBody Map map) {
        return financeComprehensiveService.page(map);
    }

    @PostMapping("pageFundSelection")
    @ApiOperation(value = "基金选择")
    public PageResult<Map> pageFundSelection(@RequestBody Map map) {
        return financeFundSelectionService.page(map);
    }

    @PostMapping("pageValuation")
    @ApiOperation(value = "估值分析")
    public PageResult<Map> pageValuation(@RequestBody Map map) {
        return moneyService.page(map);
    }

}
