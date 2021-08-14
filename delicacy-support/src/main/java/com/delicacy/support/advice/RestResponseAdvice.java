package com.delicacy.support.advice;

import com.delicacy.support.entity.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author yutao
 * @create 2020-05-13 9:23
 **/
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class RestResponseAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o == null) {
            return o;
        } else if (o instanceof ResponseResult) {
            return o;
        } else if (o instanceof Throwable) {
            return o;
        }
        return ResponseResult.ok(o);
    }


}
