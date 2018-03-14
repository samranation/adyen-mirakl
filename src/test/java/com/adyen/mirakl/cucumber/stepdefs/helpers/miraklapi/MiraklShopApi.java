package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.google.common.collect.ImmutableList;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreateShop;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import com.mirakl.client.mmp.operator.request.shop.MiraklCreateShopsRequest;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MiraklShopApi extends MiraklShopProperties {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MiraklCreateShop miraklCreateShop = new MiraklCreateShop();
    private ImmutableList.Builder<MiraklCreateShop> miraklCreateShopBuilder;

    private ImmutableList.Builder<MiraklCreateShop> miraklShopCreateBuilder(MiraklCreateShop miraklCreateShop) {
        ImmutableList.Builder<MiraklCreateShop> builder = new ImmutableList.Builder<>();
        builder.add(miraklCreateShop);
        return builder;
    }

    // Individual Shop
    public MiraklCreatedShops createShopForIndividual(MiraklMarketplacePlatformOperatorApiClient client, List<Map<String, String>> rows, String legalEntity) {
        miraklCreateShop = populateMiraklShop(rows, legalEntity);
        miraklCreateShopBuilder = miraklShopCreateBuilder(miraklCreateShop);
        return createMiraklShopRequest(client);
    }

    // Individual Shop with Bank Details
    public MiraklCreatedShops createShopForIndividualWithBankDetails(MiraklMarketplacePlatformOperatorApiClient client, List<Map<String, String>> rows, String legalEntity) {
        miraklCreateShop = populateMiraklShop(rows, legalEntity);
        miraklCreateShopBuilder = miraklShopCreateBuilder(miraklCreateShop);
        populatePaymentInformation(rows, miraklCreateShop);
        return createMiraklShopRequest(client);
    }

    // Individual Shop with full KYC data including bank account details and identity check data
    public MiraklCreatedShops createShopForIndividualWithFullKYCData(MiraklMarketplacePlatformOperatorApiClient client,  List<Map<String, String>> rows, String legalEntity) {
        miraklCreateShop = populateMiraklShop(rows, legalEntity);
        miraklCreateShopBuilder = miraklShopCreateBuilder(miraklCreateShop);
        populatePaymentInformation(rows, miraklCreateShop);
        return createMiraklShopRequest(client);
    }

    // Business with UBOs populated, amount of UBOs come from Cucumber tables
    public MiraklCreatedShops createBusinessShopWithUbos(MiraklMarketplacePlatformOperatorApiClient client, List<Map<String, String>> rows, String legalEntity) {
        miraklCreateShop = populateMiraklShop(rows, legalEntity);
        populateShareHolderData(legalEntity, rows, miraklCreateShop);
        miraklCreateShopBuilder = miraklShopCreateBuilder(miraklCreateShop);
        return createMiraklShopRequest(client);
    }

    // Mandatory for any type of shop creation
    public MiraklCreateShop populateMiraklShop(List<Map<String, String>> rows, String legalEntity){
        populateMiraklAddress(rows, miraklCreateShop);
        populateMiraklProfessionalInformation(miraklCreateShop);
        populateUserEmailAndShopName(miraklCreateShop, rows);
        populateAddFieldsLegalAndHouseNumber(legalEntity, miraklCreateShop);
        return miraklCreateShop;
    }

    public MiraklCreatedShops createMiraklShopRequest(MiraklMarketplacePlatformOperatorApiClient client) {
        MiraklCreateShopsRequest miraklCreateShopsRequest = new MiraklCreateShopsRequest(miraklCreateShopBuilder.build());
        MiraklCreatedShops shops = client.createShops(miraklCreateShopsRequest);
        throwErrorIfShopIsNotCreated(shops);
        shops.getShopReturns().forEach(MiraklCreatedShopReturn::getShopCreated);
        return shops;
    }

    private MiraklShops getAllMiraklShops(MiraklMarketplacePlatformOperatorApiClient client) {
        MiraklGetShopsRequest request = new MiraklGetShopsRequest();
        request.setPaginate(false);
        return client.getShops(request);
    }

    public MiraklShop filterMiraklShopsByEmailAndReturnShop(MiraklMarketplacePlatformOperatorApiClient client, String email) {
        MiraklShops shops = getAllMiraklShops(client);
        return shops.getShops()
            .stream().filter(shop -> shop.getContactInformation().getEmail().equalsIgnoreCase(email)).findAny()
            .orElseThrow(() -> new IllegalStateException("Shop cannot be found."));
    }
}
