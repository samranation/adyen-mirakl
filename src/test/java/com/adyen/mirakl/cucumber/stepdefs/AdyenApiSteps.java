package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.service.Notification;
import cucumber.api.java.en.Given;

import javax.annotation.Resource;


public class AdyenApiSteps extends StepDefs {

    @Resource
    private Notification adyenNotification;

    @Given("^a configuration for the notification (.*) has been created$")
    public void aConfigurationForTheNotificationEventTypeHasBeenCreated(String eventType) throws Throwable {
    }
}
