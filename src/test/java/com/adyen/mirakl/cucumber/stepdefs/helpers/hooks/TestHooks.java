package com.adyen.mirakl.cucumber.stepdefs.helpers.hooks;


import com.adyen.model.marketpay.notification.NotificationConfigurationDetails;
import org.springframework.stereotype.Component;

@Component
public class TestHooks {

    private NotificationConfigurationDetails configurationDetails;


    public NotificationConfigurationDetails getConfigurationDetails() {
        return configurationDetails;
    }

    public void setConfigurationDetails(NotificationConfigurationDetails configurationDetails) {
        this.configurationDetails = configurationDetails;
    }
}
