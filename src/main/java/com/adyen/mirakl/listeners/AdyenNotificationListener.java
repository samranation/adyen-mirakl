package com.adyen.mirakl.listeners;

import com.adyen.mirakl.config.MailTemplateService;
import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.events.AdyenNotifcationEvent;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.model.marketpay.notification.AccountHolderVerificationNotification;
import com.adyen.model.marketpay.notification.GenericNotification;
import com.adyen.notification.NotificationHandler;
import com.google.common.collect.ImmutableSet;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Locale;

@Component
public class AdyenNotificationListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private NotificationHandler notificationHandler;
    private AdyenNotificationRepository adyenNotificationRepository;
    private MailTemplateService mailTemplateService;
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;

    public AdyenNotificationListener(final NotificationHandler notificationHandler, final AdyenNotificationRepository adyenNotificationRepository, final MailTemplateService mailTemplateService, MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient) {
        this.notificationHandler = notificationHandler;
        this.adyenNotificationRepository = adyenNotificationRepository;
        this.mailTemplateService = mailTemplateService;
        this.miraklMarketplacePlatformOperatorApiClient = miraklMarketplacePlatformOperatorApiClient;
    }

    @Async
    @EventListener
    public void handleContextRefresh(AdyenNotifcationEvent event) {
        log.info(String.format("Received notification DB id: [%d]", event.getDbId()));
        final AdyenNotification notification = adyenNotificationRepository.findOneById(event.getDbId());
        final GenericNotification genericNotification = notificationHandler.handleMarketpayNotificationJson(notification.getRawAdyenNotification());
        processNotification(genericNotification);
        adyenNotificationRepository.delete(event.getDbId());
    }

    private void processNotification(final GenericNotification genericNotification) {
        if(genericNotification instanceof AccountHolderVerificationNotification){
            log.info("Sending bank verification email");
            final String shopId = ((AccountHolderVerificationNotification) genericNotification).getContent().getAccountHolderCode();
            final MiraklShop shop = getShop(shopId);
            mailTemplateService.sendMiraklShopEmailFromTemplate(shop, Locale.ENGLISH, "bankAccountVerificationEmail", "email.bank.verification.title");
        }
    }

    private MiraklShop getShop(String shopId){
        final MiraklGetShopsRequest miraklGetShopsRequest = new MiraklGetShopsRequest();
        miraklGetShopsRequest.setShopIds(ImmutableSet.of(shopId));
        final List<MiraklShop> shops = miraklMarketplacePlatformOperatorApiClient.getShops(miraklGetShopsRequest).getShops();
        if(CollectionUtils.isEmpty(shops)){
            throw new IllegalStateException("Cannot find shop: "+shopId);
        }
        return shops.iterator().next();
    }

}
