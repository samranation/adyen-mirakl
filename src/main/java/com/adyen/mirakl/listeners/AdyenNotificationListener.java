package com.adyen.mirakl.listeners;

import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.events.AdyenNotifcationEvent;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.mirakl.service.MailService;
import com.adyen.model.marketpay.notification.AccountHolderVerificationNotification;
import com.adyen.model.marketpay.notification.GenericNotification;
import com.adyen.notification.NotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdyenNotificationListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private NotificationHandler notificationHandler;
    private AdyenNotificationRepository adyenNotificationRepository;
    private MailService mailService;

    public AdyenNotificationListener(final NotificationHandler notificationHandler, final AdyenNotificationRepository adyenNotificationRepository, final MailService mailService) {
        this.notificationHandler = notificationHandler;
        this.adyenNotificationRepository = adyenNotificationRepository;
        this.mailService = mailService;
    }

    @Async
    @EventListener
    public void handleContextRefresh(AdyenNotifcationEvent event) {
        log.info(String.format("Received notification DB id: [%d]", event.getDbId()));

        final AdyenNotification notification = adyenNotificationRepository.findOneById(event.getDbId());

        final GenericNotification genericNotification = notificationHandler.handleMarketpayNotificationJson(notification.getRawAdyenNotification());

        processNotification(genericNotification);
    }

    private void processNotification(final GenericNotification genericNotification) {
        if(genericNotification instanceof AccountHolderVerificationNotification){
            log.info("Sending bank verification email");
            mailService.sendEmailFromTemplateNoUser(Locale.ENGLISH, "todoFindOutEmail", "bankAccountVerificationEmail", "email.bank.verification.title");
        }
    }

}
