package com.delicacy.support.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yutao
 * @create 2020-05-15 22:37
 **/
@Getter
@Setter
@Configuration
@ConfigurationProperties(value = "support")
public class BaseProperties {
    SwaggerProperties swagger = new SwaggerProperties();
    AdvisorProperties advisor = new AdvisorProperties();

    ResourcesProperties resources = new ResourcesProperties();
}
