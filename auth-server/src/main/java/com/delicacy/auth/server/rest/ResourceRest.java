package com.delicacy.auth.server.rest;

import com.delicacy.auth.server.constants.SecurityConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * @author yutao
 * @create 2020-08-05 14:30
 **/
@Slf4j
@RestController
@RequestMapping(value = "resource")
public class ResourceRest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${oauth.clientDetailsService}")
    private String clientDetailsServiceType;

    private JdbcClientDetailsService getJdbcClientDetailsService() {
        ClientDetailsService bean = applicationContext.getBean(ClientDetailsService.class);
        return (JdbcClientDetailsService) bean;
    }

    private InMemoryClientDetailsService getInMemoryClientDetailsService() {
        ClientDetailsService bean = applicationContext.getBean(ClientDetailsService.class);
        return (InMemoryClientDetailsService) bean;
    }


    @RequestMapping(value = "create", method = {RequestMethod.POST, RequestMethod.GET})
    public Object create(@RequestParam(required = false) String clientId,
                         @RequestParam(required = false) Integer seconds) {
        String charSequence = UUID.randomUUID().toString();
        if (ObjectUtils.isEmpty(clientId)) {
            clientId = getRandomString(4);
        }

        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId(clientId);
        clientDetails.setClientSecret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(charSequence));
        clientDetails.setAuthorizedGrantTypes(Collections.singletonList("client_credentials"));
        clientDetails.setResourceIds(Collections.singletonList("zngk-open"));
        if (seconds == null) {
            seconds = 300;
        }

        clientDetails.setAccessTokenValiditySeconds(seconds);
        clientDetails.setRefreshTokenValiditySeconds(seconds);
        clientDetails.setScope(Collections.singletonList("all"));
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("real_client_secret", charSequence);
        clientDetails.setAdditionalInformation(additionalInformation);


        JdbcClientDetailsService service = getJdbcClientDetailsService();
        service.setInsertClientDetailsSql(SecurityConstants.DEFAULT_INSERT_STATEMENT);
        service.addClientDetails(clientDetails);
        return clientDetails;
    }


    @SneakyThrows
    @RequestMapping(value = "list", method = {RequestMethod.GET})
    public Object list() {
        switch (clientDetailsServiceType) {
            case "memery":
                InMemoryClientDetailsService inMemoryClientDetailsService = getInMemoryClientDetailsService();
                Field field = inMemoryClientDetailsService.getClass().getDeclaredField("clientDetailsStore");
                field.setAccessible(true);
                Object o = field.get(inMemoryClientDetailsService);
                return objectMapper.writeValueAsString(o);
            case "jdbc":
                JdbcClientDetailsService service = getJdbcClientDetailsService();
                service.setFindClientDetailsSql(SecurityConstants.DEFAULT_FIND_STATEMENT);
                return service.listClientDetails();
            default:
                throw new RuntimeException("error");
        }
    }

    private String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(26);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


}
