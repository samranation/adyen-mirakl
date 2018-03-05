package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.google.common.collect.ImmutableList;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShopAddress;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdateShop;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShops;
import com.mirakl.client.mmp.operator.request.shop.MiraklUpdateShopsRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MiraklUpdateShopApi extends MiraklUpdateShopProperties {

    private MiraklUpdateShop miraklUpdateShop = new MiraklUpdateShop();
    private ImmutableList.Builder<MiraklUpdateShop> miraklUpdateShopBuilder;

    private ImmutableList.Builder<MiraklUpdateShop> miraklUpdateShopBuilder(MiraklUpdateShop miraklUpdateShop) {
        ImmutableList.Builder<MiraklUpdateShop> builder = new ImmutableList.Builder<>();
        builder.add(miraklUpdateShop);
        return builder;
    }

    public void updateExistingShopsContactInfoWithTableData(MiraklShop miraklShop, String shopId, MiraklMarketplacePlatformOperatorApiClient client, List<Map<Object, Object>> rows) {
        miraklUpdateShop = populateAllMandatoryFields(miraklShop, shopId);

        // update shop contact information
        MiraklShopAddress address = updateMiraklShopAddress(miraklShop, rows);
        miraklUpdateShop.setAddress(address);

        miraklUpdateShopBuilder = miraklUpdateShopBuilder(miraklUpdateShop);
        updateMiraklRequest(client, miraklUpdateShopBuilder);

    }

    public void updateShopToIncludeVATNumber(MiraklShop miraklShop, String shopId, MiraklMarketplacePlatformOperatorApiClient client) {
            miraklUpdateShop = populateAllMandatoryFields(miraklShop, shopId);

            // update VAT number:
            updateMiraklShopTaxId(miraklShop);

            miraklUpdateShopBuilder = miraklUpdateShopBuilder(miraklUpdateShop);
            updateMiraklRequest(client, miraklUpdateShopBuilder);
    }

    public void updateShopToAddBankDetails(MiraklShop miraklShop, String shopId, MiraklMarketplacePlatformOperatorApiClient client) {
        miraklUpdateShop = populateAllMandatoryFields(miraklShop, shopId);
        miraklUpdateShopBuilder = miraklUpdateShopBuilder(miraklUpdateShop);
        updateMiraklRequest(client, miraklUpdateShopBuilder);
    }

    public void updateShopsIbanNumberOnly(MiraklShop miraklShop, String shopId, MiraklMarketplacePlatformOperatorApiClient client, List<Map<Object, Object>> rows) {
            miraklUpdateShop = populateAllMandatoryFields(miraklShop, shopId);

            // update new iban number only:
            MiraklIbanBankAccountInformation paymentInformation = updateNewMiraklIbanOnly(miraklShop, rows);
            miraklUpdateShop.setPaymentInformation(paymentInformation);

            miraklUpdateShopBuilder = miraklUpdateShopBuilder(miraklUpdateShop);
            updateMiraklRequest(client, miraklUpdateShopBuilder);
    }

    // required for any update we do to Mirakl
    private MiraklUpdateShop populateAllMandatoryFields(MiraklShop miraklShop, String shopId) {
            miraklUpdateShop.setShopId(Long.valueOf(shopId));
            MiraklIbanBankAccountInformation paymentInformation = populateMiraklIbanBankAccountInformation();
            miraklUpdateShop.setPaymentInformation(paymentInformation);
            MiraklShopAddress address = populateMiraklShopAddress(miraklShop);
            miraklUpdateShop.setAddress(address);
            populateShopNameAndEmail(miraklShop, miraklUpdateShop);
            populateMiraklChannel(miraklShop, miraklUpdateShop);
            populateMiraklShopPremiumSuspendAndPaymentBlockedStatus(miraklShop, miraklUpdateShop);

            // map will be used to define additional fields that require updates/changes
            Map<String, String> fieldsToUpdate = new HashMap<>();
//            blah.put("adyen-ubo1-civility", "Mrs");
//            blah.put("adyen-individual-idnumber", "0123456789");

            populateMiraklAdditionalFields(miraklUpdateShop, miraklShop, fieldsToUpdate);
        return miraklUpdateShop;
    }

    private void updateMiraklRequest(MiraklMarketplacePlatformOperatorApiClient client, ImmutableList.Builder<MiraklUpdateShop> builder) {
        MiraklUpdateShopsRequest request = new MiraklUpdateShopsRequest(builder.build());
        final MiraklUpdatedShops miraklUpdatedShopsResponse = client.updateShops(request);
        throwErrorIfShopFailedToUpdate(miraklUpdatedShopsResponse);
    }
}

