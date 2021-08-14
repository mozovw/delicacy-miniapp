
package com.delicacy.support.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ErrorResult implements Serializable {
    private static final long serialVersionUID = 8597272892556251896L;
    private String error;
    private String message;

    public ErrorResult() {
    }

    public ErrorResult(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorResult(Integer error, String message) {
        this.error = null != error ? error.toString() : null;
        this.message = message;
    }
}
