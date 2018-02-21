package com.adyen.mirakl.cucumber.stepdefs.helpers.Hooks;

import com.adyen.model.marketpay.notification.CreateNotificationConfigurationRequest;
import com.adyen.model.marketpay.notification.NotificationConfigurationDetails;
import com.adyen.model.marketpay.notification.NotificationEventConfiguration;
import com.adyen.service.Notification;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ConfigurationProperties(prefix = "requestbin", ignoreUnknownFields = false)
public class StartUpCucumberHook implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private CucumberHooks cucumberHooks;
    @Resource
    private Notification adyenNotification;

    private String baseRequestbinUrl;
    private String baseRequestBinUrlPath;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ResponseBody body = RestAssured.post(baseRequestbinUrl.concat("api/v1/bins")).thenReturn().body();
        baseRequestBinUrlPath = baseRequestbinUrl.concat("api/v1/bins/").concat(body.jsonPath().get("name").toString()).concat("/requests");
        baseRequestbinUrl = baseRequestbinUrl.concat(body.jsonPath().get("name").toString());

        try {
            createConfigs();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create config", e);
        }
    }

    private void createConfigs() throws Exception {
        CreateNotificationConfigurationRequest createNotificationConfigurationRequest = new CreateNotificationConfigurationRequest();
        NotificationConfigurationDetails configurationDetails = new NotificationConfigurationDetails();
        configurationDetails.setActive(true);
        configurationDetails.description(baseRequestbinUrl);
        // Event Config
        NotificationEventConfiguration notificationEventConfiguration = new NotificationEventConfiguration();
        notificationEventConfiguration.setEventType(NotificationEventConfiguration.EventTypeEnum.ACCOUNT_HOLDER_CREATED);
        notificationEventConfiguration.setIncludeMode(NotificationEventConfiguration.IncludeModeEnum.INCLUDE);
        configurationDetails.setEventConfigs(ImmutableList.of(notificationEventConfiguration));

        configurationDetails.messageFormat(NotificationConfigurationDetails.MessageFormatEnum.JSON);
        configurationDetails.setNotifyURL(baseRequestbinUrl);
        configurationDetails.setSendActionHeader(true);
        configurationDetails.setSslProtocol(NotificationConfigurationDetails.SslProtocolEnum.SSL);
        createNotificationConfigurationRequest.setConfigurationDetails(configurationDetails);
        adyenNotification.createNotificationConfiguration(createNotificationConfigurationRequest);
        cucumberHooks.setConfigurationDetails(configurationDetails);
    }

    public CucumberHooks getCucumberHooks() {
        return cucumberHooks;
    }

    public void setCucumberHooks(CucumberHooks cucumberHooks) {
        this.cucumberHooks = cucumberHooks;
    }

    public Notification getAdyenNotification() {
        return adyenNotification;
    }

    public void setAdyenNotification(Notification adyenNotification) {
        this.adyenNotification = adyenNotification;
    }

    public String getBaseRequestbinUrl() {
        return baseRequestbinUrl;
    }

    public void setBaseRequestbinUrl(String baseRequestbinUrl) {
        this.baseRequestbinUrl = baseRequestbinUrl;
    }

    public String getBaseRequestBinUrlPath() {
        return baseRequestBinUrlPath;
    }

    public void setBaseRequestBinUrlPath(String baseRequestBinUrlPath) {
        this.baseRequestBinUrlPath = baseRequestBinUrlPath;
    }
}
