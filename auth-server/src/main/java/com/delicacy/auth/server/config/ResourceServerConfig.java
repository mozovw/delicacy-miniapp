package com.delicacy.auth.server.config;

import com.delicacy.auth.server.oauth.OauthExceptionTranslator;
import com.delicacy.auth.server.utils.AopUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;


@Configuration
@EnableResourceServer
@Import({JwtTokenConfig.class, CorsFilterConfig.class})
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Autowired
    private ResourceServerProperties resource;
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    @Qualifier("jwtTokenServices")
    private DefaultTokenServices defaultTokenServices;

    @SneakyThrows
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        // Invalid access token 方面异常
        OAuth2AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        authenticationEntryPoint.setExceptionTranslator(new OauthExceptionTranslator(HttpStatus.OK));
        resources.authenticationEntryPoint(authenticationEntryPoint);
        // access_denied 方面异常
        OAuth2AccessDeniedHandler oAuth2AccessDeniedHandler = new OAuth2AccessDeniedHandler();
        oAuth2AccessDeniedHandler.setExceptionTranslator(new OauthExceptionTranslator(HttpStatus.OK));
        resources.accessDeniedHandler(oAuth2AccessDeniedHandler);
        resources.tokenServices(defaultTokenServices).tokenStore(tokenStore).resourceId(resource.getResourceId());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.cors();
        http.csrf().disable();
        http.formLogin().disable();
        http.sessionManagement().disable();
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
        http.requestMatchers().antMatchers("/user/**", "/logout")
                .and().authorizeRequests().anyRequest().authenticated();

    }
}