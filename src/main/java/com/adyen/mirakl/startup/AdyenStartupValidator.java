package com.adyen.mirakl.startup;

import com.adyen.model.marketpay.notification.*;
import com.adyen.service.Notification;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties("adyenNotificationsConfig")
public class AdyenStartupValidator implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(AdyenStartupValidator.class);

    @Resource
    private Notification adyenNotification;

    private List<NotificationConfigurationDetails> notificationConfigurationDetails;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        try {
            //should not have logic in getters/setters
            //workaround as the eventConfigsContainer is not set during startup config mapping
            //application.yml does not force the setter, but tries the getter first
            //NotificationConfigurationDetails.setEventConfigs has logic in to map fields to eventConfigsContainer
            //but is not invoked as the NotificationConfigurationDetails.getEventConfigs creates a new list which we can add to
            notificationConfigurationDetails.forEach(x -> x.setEventConfigs(x.getEventConfigs()));
            sync();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sync Adyen notification configuration", e);
        }
    }

    private void sync() throws Exception {
        //map with description as unique field
        final Map<String, NotificationConfigurationDetails> currentNotificationSetup = notificationConfigurationDetails.stream()
            .collect(Collectors.toMap(NotificationConfigurationDetails::getDescription, Function.identity()));

        //filter for notification ids we care about
        final GetNotificationConfigurationListResponse notificationConfigurationList = adyenNotification.getNotificationConfigurationList();
        final Map<String, Long> descriptionsToIds = notificationConfigurationList.getConfigurations().stream()
            .filter(configurationDetail -> currentNotificationSetup.containsKey(configurationDetail.getDescription()))
            .collect(Collectors.toMap(NotificationConfigurationDetails::getDescription, NotificationConfigurationDetails::getNotificationId));

        //transfer the notification ID
        descriptionsToIds.forEach((desc, id) -> currentNotificationSetup.get(desc).setNotificationId(id));

        //update with our config
        for (NotificationConfigurationDetails configurationDetails : currentNotificationSetup.values()) {
            final UpdateNotificationConfigurationRequest updateNotificationConfigurationRequest = new UpdateNotificationConfigurationRequest();
            updateNotificationConfigurationRequest.setConfigurationDetails(configurationDetails);
            final UpdateNotificationConfigurationResponse updateNotificationConfigurationResponse = adyenNotification.updateNotificationConfiguration(updateNotificationConfigurationRequest);
            log.info(String.format("Update notification [%s]. Psp ref: [%s]",  configurationDetails,updateNotificationConfigurationResponse.getPspReference()));
        }
    }


    public Notification getAdyenNotification() {
        return adyenNotification;
    }

    public void setAdyenNotification(final Notification adyenNotification) {
        this.adyenNotification = adyenNotification;
    }

    public List<NotificationConfigurationDetails> getNotificationConfigurationDetails() {
        return notificationConfigurationDetails;
    }

    public void setNotificationConfigurationDetails(final List<NotificationConfigurationDetails> notificationConfigurationDetails) {
        this.notificationConfigurationDetails = notificationConfigurationDetails;
    }
}
