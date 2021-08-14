package com.delicacy.miniapp.respository.config;

import com.delicacy.common.factory.YamlPropertySourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ConditionalOnBean(MongoTemplate.class)
@EnableTransactionManagement
@Configuration
@PropertySource(value = {
        "classpath:application-respository.yml",
        "classpath:application-respository-${spring.profiles.active}.yml"},
        factory = YamlPropertySourceFactory.class)
public class RespositoryConfig {


}