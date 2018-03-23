package com.adyen.mirakl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "accounts", ignoreUnknownFields = false)
public class AdyenAccountConfiguration {

    private Map<String, Integer> accountCode;

    public Map<String, Integer> getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(Map<String, Integer> accountCode) {
        this.accountCode = accountCode;
    }
}
