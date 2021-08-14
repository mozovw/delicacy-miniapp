package com.delicacy.support.config;


import com.delicacy.support.aspect.LogAdvice;
import com.delicacy.support.properties.BaseProperties;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvisorConfig {

    @Autowired
    private BaseProperties baseProperties;

    @Bean
    @ConditionalOnProperty(prefix = "support.advisor", name = "restScanPackages")
    public AspectJExpressionPointcutAdvisor logAdvisor() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(baseProperties.getAdvisor().getRestScanPackages());
        advisor.setAdvice(new LogAdvice());
        return advisor;
    }

}
