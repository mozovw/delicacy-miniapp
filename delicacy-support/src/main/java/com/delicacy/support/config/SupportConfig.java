package com.delicacy.support.config;

import com.delicacy.common.factory.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application-support.yml", factory = YamlPropertySourceFactory.class)
public class SupportConfig {

}
