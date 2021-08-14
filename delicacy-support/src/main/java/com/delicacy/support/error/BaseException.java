package com.delicacy.support.error;

import com.delicacy.common.utils.ObjectUtils;
import com.delicacy.support.entity.ErrorResult;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public abstract class BaseException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 7040606850757520082L;
    protected List<ErrorResult> errors = new ArrayList<>();

    protected BaseException(String error,String message) {
        this.errors.add(new ErrorResult(error, message));
    }

    public String getMessage() {
        if (ObjectUtils.isEmpty(this.errors)) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator var2 = this.errors.iterator();

            while (var2.hasNext()) {
                ErrorResult errorInfo = (ErrorResult) var2.next();
                builder.append(errorInfo.getError()).append(":").append(errorInfo.getMessage());
                builder.append("\n");
            }

            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }

            return builder.toString();
        }
    }
}