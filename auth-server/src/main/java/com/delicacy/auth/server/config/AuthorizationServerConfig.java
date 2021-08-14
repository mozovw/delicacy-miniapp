package com.delicacy.auth.server.config;

import com.delicacy.auth.server.oauth.OauthExceptionTranslator;
import com.delicacy.auth.server.utils.AopUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@Configuration
@EnableAuthorizationServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({ClientDetailsConfig.class, JwtTokenConfig.class, CorsFilterConfig.class})
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {


    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenStore tokenStore;
    @Resource
    private UserDetailsService detailsService;
    @Resource
    private ClientDetailsService clientDetailsService;
    @Autowired
    @Qualifier("jwtTokenServices")
    private DefaultTokenServices defaultTokenServices;

    public static void main(String[] args) {
        PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String charSequence = UUID.randomUUID().toString();
        System.out.println(charSequence);
        String encode = delegatingPasswordEncoder.encode(charSequence);
        System.out.println(encode);
        boolean matches = delegatingPasswordEncoder.matches(charSequence, encode);
        System.out.println(matches);
    }

    @Override
    @SneakyThrows
    public void configure(ClientDetailsServiceConfigurer clients) {
        clients.withClientDetails(clientDetailsService);
    }


    @Override
    @SneakyThrows
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.userDetailsService(detailsService)
                .tokenStore(tokenStore)
                .exceptionTranslator(new OauthExceptionTranslator(HttpStatus.OK))
                .tokenServices(AopUtils.getTarget(defaultTokenServices))
                .authenticationManager(authenticationManager)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);

    }

    @Override
    @SneakyThrows
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        // /oauth/token 这个接口异常没有处理
        oauthServer.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients()
        ;
    }


}