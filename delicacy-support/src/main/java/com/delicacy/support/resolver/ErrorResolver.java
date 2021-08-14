package com.delicacy.support.resolver;

import com.delicacy.common.utils.JsonUtils;
import com.delicacy.common.utils.ObjectUtils;
import com.delicacy.common.utils.StringUtils;
import com.delicacy.support.error.BusinessException;
import com.delicacy.support.error.ServerException;
import com.delicacy.support.entity.ErrorResult;
import com.delicacy.support.entity.ResponseResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.delicacy.support.constants.ErrorConstants.SERVER_ERROR;
import static com.delicacy.support.constants.ErrorConstants.SERVER_ERROR_TEXT;

/**
 * @author yutao.zhang
 * @create 2021-07-30 17:42
 **/
@Slf4j
public class ErrorResolver extends DefaultHandlerExceptionResolver {

    @SneakyThrows
    protected ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request, HttpServletResponse response, @Nullable Object handler) {
        return null;
    }

    @SneakyThrows
    @Override
    @Nullable
    protected ModelAndView doResolveException(
            HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {
        ModelAndView modelAndView = super.doResolveException(request, response, handler, ex);
        if (modelAndView == null) {
            ResponseResult<Void> r = resolveError(ex);
            String s = JsonUtils.toJson(r);
            render(response, MediaType.APPLICATION_JSON_VALUE, s);
            return new ModelAndView();
        }
        return modelAndView;
    }

    private void render(HttpServletResponse response, String contentType, String content, String... headers) {
        try {
            String encoding = "UTF-8";
            boolean noCache = true;
            String[] var6 = headers;
            int var7 = headers.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                String header = var6[var8];
                String headerName = StringUtils.subBefore(header, ":");
                String headerValue = StringUtils.subAfter(header, ":");
                if (headerName.equalsIgnoreCase("encoding")) {
                    encoding = headerValue;
                } else {
                    if (!headerName.equalsIgnoreCase("no-cache")) {
                        throw new IllegalArgumentException(headerName + "不是一个合法的header类型");
                    }

                    noCache = Boolean.parseBoolean(headerValue);
                }
            }

            String fullContentType = contentType + ";charset=" + encoding;
            response.setContentType(fullContentType);
            if (noCache) {
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setDateHeader("Expires", 0L);
            }

            response.getWriter().write(content);
            response.getWriter().flush();
        } catch (IOException var12) {
            log.error(var12.getMessage(), var12);
        }

    }


    private ResponseResult<Void> resolveError(Throwable ex) {
        ResponseResult<Void> result = new ResponseResult<>();
        result.setSuccess(false);
        try {
            if (ex instanceof BusinessException) {
                for (ErrorResult error : ((BusinessException) ex).getErrors()) {
                    result.addError(error);
                }
            } else if (ex instanceof ServerException) {
                for (ErrorResult error : ((ServerException) ex).getErrors()) {
                    result.addError(error);
                }
            } else if (ex instanceof ConstraintViolationException) {
                Set<ConstraintViolation<?>> violationSet = ((ConstraintViolationException) ex).getConstraintViolations();
                if (!ObjectUtils.isEmpty(violationSet)) {
                    violationSet.forEach(v -> {
                        String name = StringUtils.subAfter(v.getConstraintDescriptor().getAnnotation().annotationType().getName(), ".");
                        result.addError(new ErrorResult(StringUtils.humpToLine(name), v.getMessage()));
                    });
                }
            } else if (ex instanceof MethodArgumentNotValidException) {
                BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
                List<FieldError> errors = bindingResult.getFieldErrors();
                if (!ObjectUtils.isEmpty(errors)) {
                    List<ErrorResult> errorInfoList = errors.stream().map(err -> {
                        String code = Objects.requireNonNull(err.getCode());
                        return new ErrorResult(StringUtils.humpToLine(code), err.getDefaultMessage());
                    }).collect(Collectors.toList());
                    result.setErrors(errorInfoList);
                }
            } else if (ex instanceof BindException) {
                BindingResult bindingResult = ((BindException) ex).getBindingResult();
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();
                if (!ObjectUtils.isEmpty(fieldErrors)) {
                    fieldErrors.forEach(fieldError -> {
                        result.addError(new ErrorResult(SERVER_ERROR, fieldError.getDefaultMessage()));
                    });
                }
            } else if (ex instanceof NestedServletException) {
                Throwable cause = ex.getCause();
                return resolveError(cause);
            } else {
                //这里是未知异常，直接报服务器错误
                result.addError(new ErrorResult(SERVER_ERROR, SERVER_ERROR_TEXT));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //此处已经获取异常信息失败了，直接返回错误消息
            result.addError(new ErrorResult(SERVER_ERROR, SERVER_ERROR_TEXT));
        }
        return result;
    }
}
