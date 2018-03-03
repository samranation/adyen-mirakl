package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.google.common.collect.ImmutableList;
import com.mirakl.client.mmp.domain.shop.MiraklShopAddress;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdateShop;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShops;
import com.mirakl.client.mmp.operator.request.shop.MiraklUpdateShopsRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MiraklUpdateShopApi extends MiraklUpdateShopProperties {

    private MiraklUpdateShop miraklUpdateShop = new MiraklUpdateShop();


    public void updateExistingShopsContactInfoWithTableData(MiraklCreatedShops createdShops, String shopId, MiraklMarketplacePlatformOperatorApiClient client, List<Map<Object, Object>> rows) {
        for (MiraklCreatedShopReturn miraklCreatedShopReturn : createdShops.getShopReturns()){
            miraklUpdateShop.setShopId(Long.valueOf(shopId));
            MiraklIbanBankAccountInformation paymentInformation = getMiraklIbanBankAccountInformation();
            miraklUpdateShop.setPaymentInformation(paymentInformation);

            // update shop contact information
            MiraklShopAddress address = setMiraklShopAddress(miraklCreatedShopReturn, rows);
            miraklUpdateShop.setAddress(address);

            getShopNameAndEmail(miraklCreatedShopReturn, miraklUpdateShop);
            getMiraklShopChannelAndPremiumStateAndSuspendAndPaymentBlockedStatus(miraklCreatedShopReturn, miraklUpdateShop);
            miraklUpdateShop.setAdditionalFieldValues(ImmutableList.of(createAdditionalField(miraklCreatedShopReturn)));
            updateMiraklRequest(client);
        }
    }

    public void updateShopToIncludeVATNumber(MiraklCreatedShops createdShops, String shopId, MiraklMarketplacePlatformOperatorApiClient client) {
        for (MiraklCreatedShopReturn miraklCreatedShopReturn : createdShops.getShopReturns()){
            miraklUpdateShop.setShopId(Long.valueOf(shopId));
            MiraklIbanBankAccountInformation paymentInformation = getMiraklIbanBankAccountInformation();
            miraklUpdateShop.setPaymentInformation(paymentInformation);
            MiraklShopAddress address = getMiraklShopAddress(miraklCreatedShopReturn);
            miraklUpdateShop.setAddress(address);

            // update VAT number:
            setMiraklShopTaxId(miraklCreatedShopReturn);

            getShopNameAndEmail(miraklCreatedShopReturn, miraklUpdateShop);
            getMiraklShopChannelAndPremiumStateAndSuspendAndPaymentBlockedStatus(miraklCreatedShopReturn, miraklUpdateShop);
            miraklUpdateShop.setAdditionalFieldValues(ImmutableList.of(createAdditionalField(miraklCreatedShopReturn)));
            updateMiraklRequest(client);
        }
    }

    public void updateShopToAddBankDetails(MiraklCreatedShops createdShops, String shopId, MiraklMarketplacePlatformOperatorApiClient client) {
        for (MiraklCreatedShopReturn miraklCreatedShopReturn : createdShops.getShopReturns()){
            miraklUpdateShop.setShopId(Long.valueOf(shopId));
            MiraklIbanBankAccountInformation paymentInformation = getMiraklIbanBankAccountInformation();
            miraklUpdateShop.setPaymentInformation(paymentInformation);
            MiraklShopAddress address = getMiraklShopAddress(miraklCreatedShopReturn);
            miraklUpdateShop.setAddress(address);
            getShopNameAndEmail(miraklCreatedShopReturn, miraklUpdateShop);
            getMiraklShopChannelAndPremiumStateAndSuspendAndPaymentBlockedStatus(miraklCreatedShopReturn, miraklUpdateShop);
            miraklUpdateShop.setAdditionalFieldValues(ImmutableList.of(createAdditionalField(miraklCreatedShopReturn)));
            updateMiraklRequest(client);
        }
    }

    public void updateShopsIbanNumberOnly(MiraklCreatedShops createdShops, String shopId, MiraklMarketplacePlatformOperatorApiClient client, List<Map<Object, Object>> rows) {
        for (MiraklCreatedShopReturn miraklCreatedShopReturn : createdShops.getShopReturns()){
            miraklUpdateShop.setShopId(Long.valueOf(shopId));

            // update new iban number only:
            MiraklIbanBankAccountInformation paymentInformation = setNewMiraklIbanOnly(miraklCreatedShopReturn, rows);
            miraklUpdateShop.setPaymentInformation(paymentInformation);

            MiraklShopAddress address = getMiraklShopAddress(miraklCreatedShopReturn);
            miraklUpdateShop.setAddress(address);
            getShopNameAndEmail(miraklCreatedShopReturn, miraklUpdateShop);
            getMiraklShopChannelAndPremiumStateAndSuspendAndPaymentBlockedStatus(miraklCreatedShopReturn, miraklUpdateShop);
            miraklUpdateShop.setAdditionalFieldValues(ImmutableList.of(createAdditionalField(miraklCreatedShopReturn)));
            updateMiraklRequest(client);
        }
    }

    private void updateMiraklRequest(MiraklMarketplacePlatformOperatorApiClient client) {
        MiraklUpdateShopsRequest request = new MiraklUpdateShopsRequest(ImmutableList.of(miraklUpdateShop));
        final MiraklUpdatedShops miraklUpdatedShopsResponse = client.updateShops(request);
        throwErrorIfShopFailedToUpdate(miraklUpdatedShopsResponse);
    }
}
