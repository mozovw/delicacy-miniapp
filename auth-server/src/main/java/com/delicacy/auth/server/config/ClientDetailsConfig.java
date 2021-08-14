package com.delicacy.auth.server.config;

import com.delicacy.auth.server.constants.SecurityConstants;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Order(0)
@Configuration
public class ClientDetailsConfig {

    @Resource
    private DataSource dataSource;

    @Bean
    @SneakyThrows
    @Primary
    @ConditionalOnProperty(prefix = "oauth",name = "clientDetailsService",havingValue = "memery")
    public ClientDetailsService memeryClientDetailsService(){
        InMemoryClientDetailsServiceBuilder clientDetailsServiceBuilder = new InMemoryClientDetailsServiceBuilder();
        clientDetailsServiceBuilder
                .withClient("maotai")
                .secret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("yuzhoudiyi"))
                .scopes("all")
                .authorizedGrantTypes("client_credentials")
                .and()
                .withClient("miniapp")
                .secret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("qwertyuiop"))
                .scopes("all")
                .authorizedGrantTypes("client_credentials");
        return clientDetailsServiceBuilder.build();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "oauth",name = "clientDetailsService",havingValue = "jdbc")
    public ClientDetailsService jdbcClientDetailsService(){
        JdbcClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        clientDetailsService.setSelectClientDetailsSql(SecurityConstants.DEFAULT_SELECT_STATEMENT);
        clientDetailsService.setFindClientDetailsSql(SecurityConstants.DEFAULT_FIND_STATEMENT);
        return clientDetailsService;
    }

}
