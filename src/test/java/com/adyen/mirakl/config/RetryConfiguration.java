package com.adyen.mirakl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RetryConfiguration {

    @Bean
    public RetryTemplate retryMailTemplate() {
        //default test retry mail template mainly used in cucumber tests
        final RetryTemplate retryMailTemplate = new RetryTemplate();
        retryMailTemplate.setRetryPolicy(new NeverRetryPolicy());
        return retryMailTemplate;
    }

}
