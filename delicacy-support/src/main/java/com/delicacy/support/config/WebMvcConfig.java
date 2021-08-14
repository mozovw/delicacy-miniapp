package com.delicacy.support.config;

import com.delicacy.support.properties.BaseProperties;
import com.delicacy.support.resolver.ErrorResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private BaseProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (properties.getResources().getLocations() == null) {
            return;
        }
        registry.addResourceHandler(properties.getResources().getPathPatterns())
                // Add one or more resource locations from which to serve static content.
                .addResourceLocations(properties.getResources().getLocations());
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        converters.add(messageConverter);
        Collections.swap(converters, converters.size() - 1, 0);
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new ErrorResolver());
    }


}