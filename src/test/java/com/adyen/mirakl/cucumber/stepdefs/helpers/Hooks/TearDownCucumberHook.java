package com.adyen.mirakl.cucumber.stepdefs.helpers.Hooks;

import com.adyen.model.marketpay.notification.DeleteNotificationConfigurationRequest;
import com.adyen.model.marketpay.notification.GetNotificationConfigurationListResponse;
import com.adyen.model.marketpay.notification.NotificationConfigurationDetails;
import com.adyen.service.Notification;
import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class TearDownCucumberHook implements ApplicationListener<ContextClosedEvent> {

    @Resource
    private CucumberHooks cucumberHooks;

    @Resource
    private StartUpCucumberHook startUpCucumberHook;

    @Resource
    private Notification adyenNotification;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        RestAssured.delete(startUpCucumberHook.getBaseRequestbinUrl().concat(startUpCucumberHook.getBaseRequestBinUrlPath()));

        try {
            removeConfigs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeConfigs() throws Exception {
        GetNotificationConfigurationListResponse notificationConfigurationList = adyenNotification.getNotificationConfigurationList();
        Optional<Long> notificationIdToDelete = notificationConfigurationList.getConfigurations().stream()
            .filter(x -> x.getNotifyURL().equalsIgnoreCase(cucumberHooks.getConfigurationDetails().getNotifyURL()))
            .map(NotificationConfigurationDetails::getNotificationId)
            .findAny();

        if (notificationIdToDelete.isPresent()) {
            DeleteNotificationConfigurationRequest deleteNotificationConfigurationRequest = new DeleteNotificationConfigurationRequest();
            deleteNotificationConfigurationRequest.setNotificationIds(ImmutableList.of(notificationIdToDelete.get()));
            adyenNotification.deleteNotificationConfiguration(deleteNotificationConfigurationRequest);
        }

    }

}

