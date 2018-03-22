package com.adyen.mirakl.listeners;

import com.adyen.mirakl.config.MailTemplateService;
import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.events.AdyenNotifcationEvent;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.model.marketpay.GetAccountHolderRequest;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.model.marketpay.KYCCheckStatusData;
import com.adyen.model.marketpay.ShareholderContact;
import com.adyen.model.marketpay.notification.AccountHolderVerificationNotification;
import com.adyen.model.marketpay.notification.GenericNotification;
import com.adyen.notification.NotificationHandler;
import com.adyen.service.Account;
import com.adyen.service.exception.ApiException;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

@Component
public class AdyenNotificationListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static Map<Map<KYCCheckStatusData.CheckTypeEnum, KYCCheckStatusData.CheckStatusEnum>, String> templateMap = ImmutableMap.of(
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA), "accountHolderAwaitingIdentityEmail",
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA), "accountHolderAwaitingPassportEmail",
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.INVALID_DATA), "accountHolderInvalidIdentityEmail",
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.INVALID_DATA), "accountHolderInvalidPassportEmail"
    );
    private static Map<Map<KYCCheckStatusData.CheckTypeEnum, KYCCheckStatusData.CheckStatusEnum>, String> subjectMap = ImmutableMap.of(
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA), "email.account.verification.awaiting.id.title",
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA), "email.account.verification.awaiting.passport.title",
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.INVALID_DATA), "email.account.verification.invalid.id.title",
        ImmutableMap.of(KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION, KYCCheckStatusData.CheckStatusEnum.INVALID_DATA), "email.account.verification.invalid.passport.title"
    );

    private NotificationHandler notificationHandler;
    private AdyenNotificationRepository adyenNotificationRepository;
    private MailTemplateService mailTemplateService;
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    private Account adyenAccountService;

    public AdyenNotificationListener(final NotificationHandler notificationHandler, final AdyenNotificationRepository adyenNotificationRepository, final MailTemplateService mailTemplateService, MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient, Account adyenAccountService) {
        this.notificationHandler = notificationHandler;
        this.adyenNotificationRepository = adyenNotificationRepository;
        this.mailTemplateService = mailTemplateService;
        this.miraklMarketplacePlatformOperatorApiClient = miraklMarketplacePlatformOperatorApiClient;
        this.adyenAccountService = adyenAccountService;
    }

    @Async
    @EventListener
    public void handleContextRefresh(AdyenNotifcationEvent event) {
        log.info(String.format("Received notification DB id: [%d]", event.getDbId()));
        final AdyenNotification notification = adyenNotificationRepository.findOneById(event.getDbId());
        final GenericNotification genericNotification = notificationHandler.handleMarketpayNotificationJson(notification.getRawAdyenNotification());
        try {
            processNotification(genericNotification);
            adyenNotificationRepository.delete(event.getDbId());
        } catch (ApiException e) {
            log.error("Failed processing notification: {}", e.getError(), e);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
        }
    }

    private void processNotification(final GenericNotification genericNotification) throws Exception {
        if(genericNotification instanceof AccountHolderVerificationNotification){
            processAccountholderVerificationNotification((AccountHolderVerificationNotification) genericNotification);
        }
    }

    private void processAccountholderVerificationNotification(final AccountHolderVerificationNotification verificationNotification) throws Exception {
        final KYCCheckStatusData.CheckStatusEnum verificationStatus = verificationNotification.getContent().getVerificationStatus();
        final KYCCheckStatusData.CheckTypeEnum verificationType = verificationNotification.getContent().getVerificationType();
        final String shopId = verificationNotification.getContent().getAccountHolderCode();
        if(KYCCheckStatusData.CheckStatusEnum.RETRY_LIMIT_REACHED.equals(verificationStatus) &&
            KYCCheckStatusData.CheckTypeEnum.BANK_ACCOUNT_VERIFICATION.equals(verificationType)){
            final MiraklShop shop = getShop(shopId);
            mailTemplateService.sendMiraklShopEmailFromTemplate(shop, Locale.ENGLISH, "bankAccountVerificationEmail", "email.bank.verification.title");
        }else if(awaitingDataForIdentityOrPassport(verificationStatus, verificationType) || invalidDataForIdentityOrPassport(verificationStatus, verificationType)){
            final GetAccountHolderRequest getAccountHolderRequest = new GetAccountHolderRequest();
            getAccountHolderRequest.setAccountHolderCode(shopId);
            final GetAccountHolderResponse accountHolderResponse = adyenAccountService.getAccountHolder(getAccountHolderRequest);
            final String shareholderCode = verificationNotification.getContent().getShareholderCode();
            final ShareholderContact shareholderContact = accountHolderResponse.getAccountHolderDetails().getBusinessDetails().getShareholders().stream()
                .filter(x -> x.getShareholderCode().equals(shareholderCode))
                .findAny().orElseThrow(() -> new IllegalStateException("Unable to find shareholder: " + shareholderCode));
            mailTemplateService.sendShareholderEmailFromTemplate(shareholderContact, shopId, Locale.ENGLISH, templateMap.get(ImmutableMap.of(verificationType, verificationStatus)), subjectMap.get(ImmutableMap.of(verificationType, verificationStatus)));
        }
    }

    private boolean awaitingDataForIdentityOrPassport(final KYCCheckStatusData.CheckStatusEnum verificationStatus, final KYCCheckStatusData.CheckTypeEnum verificationType) {
        return KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA.equals(verificationStatus)
            && (KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION.equals(verificationType) ||  KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION.equals(verificationType));
    }

    private boolean invalidDataForIdentityOrPassport(final KYCCheckStatusData.CheckStatusEnum verificationStatus, final KYCCheckStatusData.CheckTypeEnum verificationType) {
        return KYCCheckStatusData.CheckStatusEnum.INVALID_DATA.equals(verificationStatus)
            && (KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION.equals(verificationType) ||  KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION.equals(verificationType));
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
