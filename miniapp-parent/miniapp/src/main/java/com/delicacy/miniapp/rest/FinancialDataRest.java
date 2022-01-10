package com.delicacy.miniapp.rest;


import com.delicacy.miniapp.service.entity.PageResult;
import com.delicacy.miniapp.service.service.finance.*;
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
    private ComprehensiveService financeComprehensiveService;

    @Autowired
    private ValuationService moneyService;

    @Autowired
    private PegService pegService;

    @Autowired
    private TopHoldersSerivice topHolderSerivice;

    @Autowired
    private SkHolderSerivice skHolderSerivice;

    @PostMapping("pageComprehensive")
    @ApiOperation(value = "综合分析")
    public PageResult<Map> pageComprehensive(@RequestBody Map map) {
        return financeComprehensiveService.page(map);
    }

    @PostMapping("pageValuation")
    @ApiOperation(value = "估值分析")
    public PageResult<Map> pageValuation(@RequestBody Map map) {
        return moneyService.page(map);
    }


    @PostMapping("pagePEG")
    @ApiOperation(value = "PEG分析")
    public PageResult<Map> pagePEG(@RequestBody Map map) {
        return pegService.page(map);
    }


    @PostMapping("pageSkHolder")
    @ApiOperation(value = "高管分析")
    public PageResult<Map> pageSkHolder(@RequestBody Map map) {
        return skHolderSerivice.page(map);
    }


    @PostMapping("pageTopHolders")
    @ApiOperation(value = "股东分析")
    public PageResult<Map> pageTopHolders(@RequestBody Map map) {
        return topHolderSerivice.page(map);
    }

}
