package com.adyen.mirakl.config;

import com.adyen.mirakl.aop.FailSafeEmailAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class EmailAspectConfiguration {

    @Bean
    public FailSafeEmailAspect failSafeEmailAspect() {
        return new FailSafeEmailAspect();
    }
}
