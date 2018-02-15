package com.adyen.mirakl.config;

import io.github.jhipster.config.JHipsterConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class AdyenLiveConfiguration extends AbstractAdyenConfiguration {

}
