
package com.delicacy.auth.server.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ErrorResult implements Serializable {
    private static final long serialVersionUID = 8597272892556251896L;
    private String code;
    private String message;

    public ErrorResult() {
    }

    public ErrorResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResult(Integer code, String message) {
        this.code = null != code ? code.toString() : null;
        this.message = message;
    }
}
