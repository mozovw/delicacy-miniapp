

package com.delicacy.support.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ResponseResult<T> implements Serializable {
    private boolean success;
    private List<ErrorResult> errors;
    private T data;

    public ResponseResult() {
        this.success = true;
    }

    public static <T> ResponseResult ok() {
        return new ResponseResult(true);
    }

    public static <T> ResponseResult ok(T data) {
        return new ResponseResult(true, data);
    }

    public static <T> ResponseResult fail(String code, String message) {
        ResponseResult r = new ResponseResult(false);
        r.addError(code, message);
        return r;
    }

    public static <T> ResponseResult fail(List<ErrorResult> errors) {
        ResponseResult r = new ResponseResult(false);
        r.addErrors(errors);
        return r;
    }


    public ResponseResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public ResponseResult(T data) {
        this.success = true;
        this.data = data;
    }

    public void addError(ErrorResult errorInfo) {
        this.success = false;
        if (this.errors == null) {
            this.errors = new ArrayList();
        }

        this.errors.add(errorInfo);
    }

    private void addErrors(List<ErrorResult> errorInfoList) {
        if (errorInfoList != null && errorInfoList.size() > 0) {
            Iterator var2 = errorInfoList.iterator();
            while (var2.hasNext()) {
                ErrorResult errorInfo = (ErrorResult) var2.next();
                this.addError(errorInfo);
            }
        }

    }

    private void addError(String errorCode, String errorMsg) {
        ErrorResult errorInfo = new ErrorResult(errorCode, errorMsg);
        this.addError(errorInfo);
    }

}
