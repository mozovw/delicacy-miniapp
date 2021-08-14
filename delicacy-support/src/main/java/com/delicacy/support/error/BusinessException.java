package com.delicacy.support.error;

import com.delicacy.support.constants.ErrorConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class BusinessException extends BaseException implements Serializable {

    private static final long serialVersionUID = -4367795902768756325L;

    public BusinessException() {
        this(ErrorConstants.SERVER_ERROR_TEXT);
    }

    public BusinessException(String message) {
        this(ErrorConstants.BIZ_ERROR, message);
    }

    public BusinessException(String error, String message) {
        super(error, message);
    }


}
