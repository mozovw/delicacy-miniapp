package com.delicacy.miniapp.rest;

import com.delicacy.support.annotation.NotEmpty;
import com.delicacy.support.error.BusinessException;
import io.swagger.annotations.Api;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author yutao.zhang
 * @create 2021-07-30 17:58
 **/
@Slf4j
@Validated
@RestController
@RequestMapping("/result/test")
@Api(value = "/result/test", tags = "返回结果测试")
public class ResultTestRest {


    @GetMapping("str")
    public String str(String str) {
        return str;
    }

    @GetMapping("integer")
    public Integer integer() {
        return 1;
    }

    @GetMapping("nul")
    public void nul(String str) {

    }

    @GetMapping("valid")
    public String valid(@NotEmpty(message = "str required") String str, @Size(min = 2, message = "min requires 2") String str2) {
        return str;
    }

    @PostMapping("validEntry")
    public ValidEntry validEntry(@RequestBody @Valid ValidEntry e) {
        return e;
    }

    @GetMapping("err")
    public void err() {
        throw new RuntimeException("error");
    }

    @GetMapping("bizerr")
    public void bizerr() {
        throw new BusinessException("业务有误");
    }


    @Data
    static class ValidEntry implements Serializable {
        private static final long serialVersionUID = -5364518187725507440L;
        @NotEmpty(message = "msg required")
        private String msg;

    }

}
