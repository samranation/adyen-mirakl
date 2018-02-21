package com.adyen.mirakl.cucumber.stepdefs.helpers.Hooks;


import com.adyen.model.marketpay.notification.NotificationConfigurationDetails;
import org.springframework.stereotype.Component;

@Component
public class CucumberHooks {

    private NotificationConfigurationDetails configurationDetails;


    public NotificationConfigurationDetails getConfigurationDetails() {
        return configurationDetails;
    }

    public void setConfigurationDetails(NotificationConfigurationDetails configurationDetails) {
        this.configurationDetails = configurationDetails;
    }
}
