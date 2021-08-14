

package com.delicacy.auth.server.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {
    private boolean success;
    private List<E> errors;
    private T data;

    public R() {
        this.success = true;
    }

    public static <T> R ok() {
        return new R(true);
    }

    public static <T> R ok(T data) {
        return new R(true, data);
    }

    public static <T> R fail(String code, String message) {
        R r = new R(false);
        r.addError(code, message);
        return r;
    }

    public static <T> R fail(List<E> errors) {
        R r = new R(false);
        r.addErrors(errors);
        return r;
    }


    public R(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public R(T data) {
        this.success = true;
        this.data = data;
    }

    public void addError(E errorInfo) {
        this.success = false;
        if (this.errors == null) {
            this.errors = new ArrayList();
        }

        this.errors.add(errorInfo);
    }

    private void addErrors(List<E> errorInfoList) {
        if (errorInfoList != null && errorInfoList.size() > 0) {
            Iterator var2 = errorInfoList.iterator();
            while (var2.hasNext()) {
                E errorInfo = (E) var2.next();
                this.addError(errorInfo);
            }
        }

    }

    private void addError(String errorCode, String errorMsg) {
        E errorInfo = new E(errorCode, errorMsg);
        this.addError(errorInfo);
    }

}
