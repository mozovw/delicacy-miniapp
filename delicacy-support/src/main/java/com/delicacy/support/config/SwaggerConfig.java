package com.delicacy.support.config;

import com.delicacy.support.properties.BaseProperties;
import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Profile(value = {"dev", "test"})
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
@ConditionalOnClass(SpringfoxWebMvcConfiguration.class)
public class SwaggerConfig {
    @Autowired
    private BaseProperties properties;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(properties.getSwagger().getBasePackage()))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * http://localhost:8080/doc.html
     * http://localhost:8080/swagger-ui.html
     */
    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(properties.getSwagger().getTitle())
                .description(properties.getSwagger().getDescription())
                .termsOfServiceUrl(properties.getSwagger().getTermsOfServiceUrl())
                .contact(new Contact(properties.getSwagger().getName(), properties.getSwagger().getUrl(), properties.getSwagger().getEmail()))
                .version(properties.getSwagger().getVersion())
                .build();
    }


}