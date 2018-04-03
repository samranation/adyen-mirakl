package com.adyen.mirakl.listeners;

import com.adyen.mirakl.service.MailTemplateService;
import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.events.AdyenNotifcationEvent;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.mirakl.service.RetryPayoutService;
import com.adyen.model.marketpay.GetAccountHolderRequest;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.model.marketpay.KYCCheckStatusData;
import com.adyen.model.marketpay.ShareholderContact;
import com.adyen.model.marketpay.notification.AccountHolderPayoutNotification;
import com.adyen.model.marketpay.notification.AccountHolderStatusChangeNotification;
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.adyen.mirakl.listeners.AdyenNotificationListener.TemplateAndSubjectKey.getSubject;
import static com.adyen.mirakl.listeners.AdyenNotificationListener.TemplateAndSubjectKey.getTemplate;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Component
public class AdyenNotificationListener {

    static class TemplateAndSubjectKey {

        private static final Map<String, Map<String, String>> keys;

        static {
            Map<String, Map<String, String>> builder = new HashMap<>();
            builder.put(KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION.toString() + KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA.toString(),
                        ImmutableMap.of("accountHolderAwaitingIdentityEmail", "email.account.verification.awaiting.id.title"));
            builder.put(KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION.toString() + KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA.toString(),
                        ImmutableMap.of("accountHolderAwaitingPassportEmail", "email.account.verification.awaiting.passport.title"));
            builder.put(KYCCheckStatusData.CheckTypeEnum.COMPANY_VERIFICATION.toString() + KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA.toString(),
                        ImmutableMap.of("companyAwaitingIdData", "email.company.verification.awaiting.id.title"));
            builder.put(KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION.toString() + KYCCheckStatusData.CheckStatusEnum.INVALID_DATA.toString(),
                        ImmutableMap.of("accountHolderInvalidIdentityEmail", "email.account.verification.invalid.id.title"));
            builder.put(KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION.toString() + KYCCheckStatusData.CheckStatusEnum.INVALID_DATA.toString(),
                        ImmutableMap.of("accountHolderInvalidPassportEmail", "email.account.verification.invalid.passport.title"));
            builder.put(KYCCheckStatusData.CheckTypeEnum.COMPANY_VERIFICATION.toString() + KYCCheckStatusData.CheckStatusEnum.INVALID_DATA.toString(),
                        ImmutableMap.of("companyInvalidIdData", "email.company.verification.invalid.id.title"));
            keys = builder;
        }

        static String getTemplate(KYCCheckStatusData.CheckTypeEnum type, KYCCheckStatusData.CheckStatusEnum status) {
            return keys.get(type.toString() + status.toString()).keySet().iterator().next();
        }

        static String getSubject(KYCCheckStatusData.CheckTypeEnum type, KYCCheckStatusData.CheckStatusEnum status) {
            return keys.get(type.toString() + status.toString()).values().iterator().next();
        }
    }

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private NotificationHandler notificationHandler;
    private AdyenNotificationRepository adyenNotificationRepository;
    private MailTemplateService mailTemplateService;
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    private RetryPayoutService retryPayoutService;
    private Account adyenAccountService;

    AdyenNotificationListener(final NotificationHandler notificationHandler,
                              final AdyenNotificationRepository adyenNotificationRepository,
                              final MailTemplateService mailTemplateService,
                              final MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient,
                              final Account adyenAccountService,
                              final RetryPayoutService retryPayoutService) {
        this.notificationHandler = notificationHandler;
        this.adyenNotificationRepository = adyenNotificationRepository;
        this.mailTemplateService = mailTemplateService;
        this.miraklMarketplacePlatformOperatorApiClient = miraklMarketplacePlatformOperatorApiClient;
        this.adyenAccountService = adyenAccountService;
        this.retryPayoutService = retryPayoutService;
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
        if (genericNotification instanceof AccountHolderVerificationNotification) {
            processAccountholderVerificationNotification((AccountHolderVerificationNotification) genericNotification);
        }
        if (genericNotification instanceof AccountHolderStatusChangeNotification) {
            processAccountholderStatusChangeNotification((AccountHolderStatusChangeNotification) genericNotification);
        }
        if (genericNotification instanceof AccountHolderPayoutNotification) {
            processAccountHolderPayout((AccountHolderPayoutNotification) genericNotification);
        }
    }

    private void processAccountholderVerificationNotification(final AccountHolderVerificationNotification verificationNotification) throws Exception {
        final KYCCheckStatusData.CheckStatusEnum verificationStatus = verificationNotification.getContent().getVerificationStatus();
        final KYCCheckStatusData.CheckTypeEnum verificationType = verificationNotification.getContent().getVerificationType();
        final String shopId = verificationNotification.getContent().getAccountHolderCode();
        if (KYCCheckStatusData.CheckStatusEnum.RETRY_LIMIT_REACHED.equals(verificationStatus) && KYCCheckStatusData.CheckTypeEnum.BANK_ACCOUNT_VERIFICATION.equals(verificationType)) {
            final MiraklShop shop = getShop(shopId);
            mailTemplateService.sendMiraklShopEmailFromTemplate(shop, Locale.getDefault(), "bankAccountVerificationEmail", "email.bank.verification.title");
        } else if (awaitingDataForIdentityOrPassport(verificationStatus, verificationType) || invalidDataForIdentityOrPassport(verificationStatus, verificationType)) {
            final GetAccountHolderRequest getAccountHolderRequest = new GetAccountHolderRequest();
            getAccountHolderRequest.setAccountHolderCode(shopId);
            final GetAccountHolderResponse accountHolderResponse = adyenAccountService.getAccountHolder(getAccountHolderRequest);
            final String shareholderCode = verificationNotification.getContent().getShareholderCode();
            final ShareholderContact shareholderContact = accountHolderResponse.getAccountHolderDetails()
                                                                               .getBusinessDetails()
                                                                               .getShareholders()
                                                                               .stream()
                                                                               .filter(x -> x.getShareholderCode().equals(shareholderCode))
                                                                               .findAny()
                                                                               .orElseThrow(() -> new IllegalStateException("Unable to find shareholder: " + shareholderCode));
            mailTemplateService.sendShareholderEmailFromTemplate(shareholderContact,
                                                                 shopId,
                                                                 Locale.getDefault(),
                                                                 getTemplate(verificationType, verificationStatus),
                                                                 getSubject(verificationType, verificationStatus));
        } else if (invalidOrAwitingCompanyVerificationData(verificationStatus, verificationType)) {
            final MiraklShop shop = getShop(shopId);
            mailTemplateService.sendMiraklShopEmailFromTemplate(shop, Locale.getDefault(), getTemplate(verificationType, verificationStatus), getSubject(verificationType, verificationStatus));
        }
    }

    private boolean invalidOrAwitingCompanyVerificationData(final KYCCheckStatusData.CheckStatusEnum verificationStatus, final KYCCheckStatusData.CheckTypeEnum verificationType) {
        return KYCCheckStatusData.CheckTypeEnum.COMPANY_VERIFICATION.equals(verificationType) && (KYCCheckStatusData.CheckStatusEnum.INVALID_DATA.equals(verificationStatus)
            || KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA.equals(verificationStatus));
    }

    private boolean awaitingDataForIdentityOrPassport(final KYCCheckStatusData.CheckStatusEnum verificationStatus, final KYCCheckStatusData.CheckTypeEnum verificationType) {
        return KYCCheckStatusData.CheckStatusEnum.AWAITING_DATA.equals(verificationStatus) && (KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION.equals(verificationType)
            || KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION.equals(verificationType));
    }

    private boolean invalidDataForIdentityOrPassport(final KYCCheckStatusData.CheckStatusEnum verificationStatus, final KYCCheckStatusData.CheckTypeEnum verificationType) {
        return KYCCheckStatusData.CheckStatusEnum.INVALID_DATA.equals(verificationStatus) && (KYCCheckStatusData.CheckTypeEnum.IDENTITY_VERIFICATION.equals(verificationType)
            || KYCCheckStatusData.CheckTypeEnum.PASSPORT_VERIFICATION.equals(verificationType));
    }

    private MiraklShop getShop(String shopId) {
        final MiraklGetShopsRequest miraklGetShopsRequest = new MiraklGetShopsRequest();
        miraklGetShopsRequest.setShopIds(ImmutableSet.of(shopId));
        final List<MiraklShop> shops = miraklMarketplacePlatformOperatorApiClient.getShops(miraklGetShopsRequest).getShops();
        if (CollectionUtils.isEmpty(shops)) {
            throw new IllegalStateException("Cannot find shop: " + shopId);
        }
        return shops.iterator().next();
    }


    private void processAccountholderStatusChangeNotification(final AccountHolderStatusChangeNotification accountHolderStatusChangeNotification) {
        final Boolean oldPayoutState = accountHolderStatusChangeNotification.getContent().getOldStatus().getPayoutState().getAllowPayout();
        final Boolean newPayoutState = accountHolderStatusChangeNotification.getContent().getNewStatus().getPayoutState().getAllowPayout();

        if (FALSE.equals(oldPayoutState) && TRUE.equals(newPayoutState)) {
            mailTemplateService.sendMiraklShopEmailFromTemplate(getShop(accountHolderStatusChangeNotification.getContent().getAccountHolderCode()),
                                                                Locale.getDefault(),
                                                                "nowPayable",
                                                                "email.account.status.now.true.title");
        } else if (TRUE.equals(oldPayoutState) && FALSE.equals(newPayoutState)) {
            mailTemplateService.sendMiraklShopEmailFromTemplate(getShop(accountHolderStatusChangeNotification.getContent().getAccountHolderCode()),
                                                                Locale.getDefault(),
                                                                "payoutRevoked",
                                                                "email.account.status.now.false.title");
        }

        if (FALSE.equals(oldPayoutState) && TRUE.equals(newPayoutState)) {
            // check if there are payout errors to retrigger
            String accountHolderCode = accountHolderStatusChangeNotification.getContent().getAccountHolderCode();
            retryPayoutService.retryFailedPayoutsForAccountHolder(accountHolderCode);
        }
    }

    private void processAccountHolderPayout(final AccountHolderPayoutNotification accountHolderPayoutNotification) {
        if (accountHolderPayoutNotification.getContent().getStatus().getStatusCode().equals("Failed")) {
            mailTemplateService.sendOperatorEmailPayoutFailure(getShop(accountHolderPayoutNotification.getContent().getAccountHolderCode()), accountHolderPayoutNotification.getContent().getStatus().getMessage());
        }
    }
}
