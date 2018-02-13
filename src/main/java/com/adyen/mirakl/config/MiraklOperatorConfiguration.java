package com.adyen.mirakl.config;

import com.mirakl.client.core.security.MiraklCredential;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "miraklOperator", ignoreUnknownFields = false)
public class MiraklOperatorConfiguration extends MiraklProperties {

    @Bean
    public MiraklCredential miraklOperatorCredential(){
        return new MiraklCredential(getMiraklApiKey());
    }

    @Bean
    public MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient(){
        return new MiraklMarketplacePlatformOperatorApiClient(getMiraklEnvUrl(), miraklOperatorCredential());
    }

}
