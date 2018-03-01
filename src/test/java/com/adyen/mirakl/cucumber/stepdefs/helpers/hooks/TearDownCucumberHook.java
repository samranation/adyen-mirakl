package com.adyen.mirakl.cucumber.stepdefs.helpers.hooks;

import com.adyen.model.marketpay.notification.DeleteNotificationConfigurationRequest;
import com.adyen.service.Notification;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TearDownCucumberHook implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(TearDownCucumberHook.class);

    @Resource
    private StartUpCucumberHook startUpCucumberHook;

    @Resource
    private Notification adyenNotification;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            removeConfigs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeConfigs() throws Exception {
        final Long notificationId = startUpCucumberHook.getNotificationId();
        log.info("Deleting notification configuration: {}",notificationId);
        DeleteNotificationConfigurationRequest deleteNotificationConfigurationRequest = new DeleteNotificationConfigurationRequest();
        deleteNotificationConfigurationRequest.setNotificationIds(ImmutableList.of(notificationId));
        adyenNotification.deleteNotificationConfiguration(deleteNotificationConfigurationRequest);
    }

}

