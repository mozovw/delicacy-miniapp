package com.delicacy.miniapp.service.config;

import com.delicacy.common.factory.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
        "classpath:application-service.yml",
        "classpath:application-service-${spring.profiles.active}.yml"},
        factory = YamlPropertySourceFactory.class)
public class ServiceConfig {

}