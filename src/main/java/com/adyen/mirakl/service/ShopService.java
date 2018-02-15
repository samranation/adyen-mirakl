package com.adyen.mirakl.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.adyen.mirakl.config.MiraklFrontApiClientFactory;
import com.adyen.mirakl.startup.StartupValidator.CustomMiraklFields;
import com.adyen.model.Name;
import com.adyen.model.marketpay.AccountHolderDetails;
import com.adyen.model.marketpay.BusinessDetails;
import com.adyen.model.marketpay.CreateAccountHolderRequest;
import com.adyen.model.marketpay.CreateAccountHolderRequest.LegalEntityEnum;
import com.adyen.model.marketpay.ShareholderContact;
import com.mirakl.client.mmp.domain.additionalfield.MiraklAdditionalFieldType;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklContactInformation;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.front.core.MiraklMarketplacePlatformFrontApi;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class ShopService {
    private final Logger log = LoggerFactory.getLogger(ShopService.class);

    private final MiraklFrontApiClientFactory miraklFrontApiClientFactory;

    public ShopService(MiraklFrontApiClientFactory miraklFrontApiClientFactory) {
        this.miraklFrontApiClientFactory = miraklFrontApiClientFactory;
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 * * * * ?")
    public void retrievedUpdatedShops() {
        MiraklShops shops = getUpdatedShops();

        log.info("Shops" + shops.getTotalCount());
    }

    private MiraklShops getUpdatedShops() {
        MiraklMarketplacePlatformFrontApi client = miraklFrontApiClientFactory.createMiraklMarketplacePlatformFrontApiClient();

        MiraklGetShopsRequest request = new MiraklGetShopsRequest();
        return client.getShops(request);
    }

    public CreateAccountHolderRequest createAccountHolderRequestFromShop(MiraklShop shop) {
        CreateAccountHolderRequest request = new CreateAccountHolderRequest();
        request.setAccountHolderCode(shop.getId());
        MiraklValueListAdditionalFieldValue additionalFieldValue = (MiraklValueListAdditionalFieldValue) shop.getAdditionalFieldValues()
                                                                                                             .stream()
                                                                                                             .filter(field -> isListWithCode(field, CustomMiraklFields.ADYEN_LEGAL_ENTITY_TYPE))
                                                                                                             .findAny()
                                                                                                             .orElseThrow(() -> new RuntimeException("Legal entity not found"));

        LegalEntityEnum legalEntity = Arrays.stream(LegalEntityEnum.values())
                                            .filter(legalEntityEnum -> legalEntityEnum.toString().equalsIgnoreCase(additionalFieldValue.getValue()))
                                            .findAny()
                                            .orElseThrow(() -> new RuntimeException("Invalid legal entity: " + additionalFieldValue.toString()));

        request.setLegalEntity(legalEntity);
        AccountHolderDetails accountHolderDetails = new AccountHolderDetails();
        BusinessDetails businessDetails = new BusinessDetails();
        List<ShareholderContact> shareholders = new ArrayList<>();

        shareholders.add(createShareholderContactFromShop(shop));
        businessDetails.setShareholders(shareholders);

        accountHolderDetails.setBusinessDetails(businessDetails);
        request.setAccountHolderDetails(accountHolderDetails);
        return request;
    }

    private ShareholderContact createShareholderContactFromShop(MiraklShop shop) {
        ShareholderContact shareholderContact = new ShareholderContact();
        MiraklContactInformation contactInformation = shop.getContactInformation();   //todo: NPE check

        shareholderContact.setEmail(contactInformation.getEmail());
        Name name = new Name();
        name.setFirstName(contactInformation.getFirstname());
        name.setLastName(contactInformation.getLastname());
        shareholderContact.setName(name);
        return shareholderContact;
    }

    private boolean isListWithCode(MiraklAdditionalFieldValue additionalFieldValue, CustomMiraklFields field) {
        return MiraklAdditionalFieldType.LIST.equals(additionalFieldValue.getFieldType()) && field.toString().equalsIgnoreCase(additionalFieldValue.getCode());
    }
}
