package com.adyen.mirakl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Adyen Mirakl Connector.
 * <p>
 * Properties are configured in the application.yml file. See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String shopUpdaterCron;
    private String emailRetryCron;
    private String removeSentEmailsCron;
    private String docsUpdaterCron;
    private String payoutRetryCron;

    private Integer initialDeltaDaysBack;
    private Integer maxPayoutFailed;

    public String getDocsUpdaterCron() {
        return docsUpdaterCron;
    }

    public void setDocsUpdaterCron(final String docsUpdaterCron) {
        this.docsUpdaterCron = docsUpdaterCron;
    }

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

    public String getRemoveSentEmailsCron() {
        return removeSentEmailsCron;
    }

    public void setRemoveSentEmailsCron(final String removeSentEmailsCron) {
        this.removeSentEmailsCron = removeSentEmailsCron;
    }

    public String getPayoutRetryCron() {
        return payoutRetryCron;
    }

    public void setPayoutRetryCron(String payoutRetryCron) {
        this.payoutRetryCron = payoutRetryCron;
    }

    public Integer getInitialDeltaDaysBack() {
        return initialDeltaDaysBack;
    }

    public void setInitialDeltaDaysBack(Integer initialDeltaDaysBack) {
        this.initialDeltaDaysBack = initialDeltaDaysBack;
    }

    public Integer getMaxPayoutFailed() {
        return maxPayoutFailed;
    }

    public void setMaxPayoutFailed(Integer maxPayoutFailed) {
        this.maxPayoutFailed = maxPayoutFailed;
    }
}
