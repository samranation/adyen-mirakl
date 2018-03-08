package com.adyen.mirakl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Adyen Mirakl Connector.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String shopUpdaterCron;
    private String emailRetryCron;

    public String getShopUpdaterCron() {
        return shopUpdaterCron;
    }

    public void setShopUpdaterCron(final String shopUpdaterCron) {
        this.shopUpdaterCron = shopUpdaterCron;
    }

    public String getEmailRetryCron() {
        return emailRetryCron;
    }

    public void setEmailRetryCron(final String emailRetryCron) {
        this.emailRetryCron = emailRetryCron;
    }
}
