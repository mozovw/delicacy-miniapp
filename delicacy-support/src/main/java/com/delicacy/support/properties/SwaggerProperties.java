package com.delicacy.support.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yutao
 * @create 2020-05-15 22:39
 **/
@Getter
@Setter
public class SwaggerProperties {

    private String[] excludePath;
    private String basePackage;
    private String title;
    private String description;
    private String termsOfServiceUrl;
    private String name;
    private String url;
    private String email;
    private String version;

}
