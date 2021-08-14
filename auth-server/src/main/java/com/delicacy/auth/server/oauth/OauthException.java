
package com.delicacy.auth.server.oauth;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

@Getter
@JsonSerialize(using = OauthExceptionSerializer.class)
public class OauthException extends OAuth2Exception {

    private String oAuth2ErrorCode;

    private int httpErrorCode;

    public OauthException(String msg, String oAuth2ErrorCode, int httpErrorCode) {
        super(msg);
        this.oAuth2ErrorCode = oAuth2ErrorCode;
        this.httpErrorCode = httpErrorCode;
    }
}