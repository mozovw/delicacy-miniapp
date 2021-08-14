package com.delicacy.auth.server.oauth;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;

/**
 * @author yutao
 * @create 2020-05-19 10:38
 **/
@NoArgsConstructor
@AllArgsConstructor
public class OauthExceptionTranslator extends DefaultWebResponseExceptionTranslator {

    private HttpStatus statusCode;
    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
        ResponseEntity<OAuth2Exception> translate = super.translate(e);
        OAuth2Exception body = translate.getBody();
        OauthException customOauthException = new OauthException(body.getMessage(), body.getOAuth2ErrorCode(), body.getHttpErrorCode());
        HttpStatus statusCode = this.statusCode == null ? translate.getStatusCode() : this.statusCode;
        ResponseEntity<OAuth2Exception> response = new ResponseEntity<>(customOauthException, translate.getHeaders(),
                statusCode);
        return response;

    }

}
