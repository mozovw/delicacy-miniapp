package com.delicacy.support.error;

import com.delicacy.support.constants.ErrorConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ServerException extends BaseException implements Serializable {

    private static final long serialVersionUID = 4559467027454688251L;

    public ServerException() {
        this(ErrorConstants.SERVER_ERROR_TEXT);
    }

    public ServerException(String message) {
        this(ErrorConstants.BIZ_ERROR, message);
    }

    public ServerException(String error, String message) {
        super(error, message);
    }
}
