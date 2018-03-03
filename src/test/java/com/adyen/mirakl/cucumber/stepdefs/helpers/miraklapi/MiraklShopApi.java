package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.mirakl.client.domain.common.error.ErrorBean;
import com.mirakl.client.domain.common.error.InputWithErrors;
import com.mirakl.client.mmp.domain.shop.*;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdateShop;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShops;
import com.mirakl.client.mmp.operator.request.shop.MiraklCreateShopsRequest;
import com.mirakl.client.mmp.operator.request.shop.MiraklUpdateShopsRequest;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MiraklShopApi extends MiraklShopProperties {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static Gson GSON = new Gson();

    public MiraklCreatedShops createNewShops(MiraklMarketplacePlatformOperatorApiClient client, List<Map<Object, Object>> rows, boolean createShopHolderData, boolean createTaxId) {

        MiraklCreateShopsRequest miraklShopRequest = createMiraklShopRequest(rows, createShopHolderData, createTaxId);
        MiraklCreatedShops shops = client.createShops(miraklShopRequest);

        MiraklCreatedShopReturn miraklCreatedShopReturn = shops.getShopReturns()
            .stream()
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No Shop found"));

        if (miraklCreatedShopReturn.getShopCreated() == null) {
            throw new IllegalStateException(miraklCreatedShopReturn.getShopError().getErrors().toString());
        }
        String shopId = shops.getShopReturns().iterator().next().getShopCreated().getId();
        log.info(String.format("Mirakl Shop Id: [%s]", shopId));

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

    public MiraklUpdatedShops updateExistingShop(MiraklCreatedShops createdShops, String shopId, MiraklMarketplacePlatformOperatorApiClient client, String iban, boolean createTaxId) {
        Faker faker = new Faker();
        MiraklUpdateShop miraklUpdateShop = new MiraklUpdateShop();
        miraklUpdateShop.setShopId(Long.valueOf(shopId));

        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();

        //update requires bank details for some reason
        paymentInformation.setIban(iban);
        paymentInformation.setBic(faker.finance().bic());
        paymentInformation.setOwner(faker.name().firstName() + " " + faker.name().lastName());
        paymentInformation.setBankName("RBS");


        miraklUpdateShop.setPaymentInformation(paymentInformation);

        MiraklShopAddress address = new MiraklShopAddress();

        for (MiraklCreatedShopReturn miraklCreatedShopReturn : createdShops.getShopReturns()) {

            miraklUpdateShop.setName(miraklCreatedShopReturn.getShopCreated().getName());
            address.setCity(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCity());
            address.setCivility(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCivility());
            address.setCountry(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCountry());
            address.setFirstname(miraklCreatedShopReturn.getShopCreated().getContactInformation().getFirstname());
            address.setLastname(miraklCreatedShopReturn.getShopCreated().getContactInformation().getLastname());
            address.setStreet1(miraklCreatedShopReturn.getShopCreated().getContactInformation().getStreet1());
            address.setZipCode(miraklCreatedShopReturn.getShopCreated().getContactInformation().getZipCode());

            if (createTaxId) {
                miraklCreatedShopReturn.getShopCreated().getProfessionalInformation().setTaxIdentificationNumber("GB"+ RandomStringUtils.randomNumeric(9));
            }

            miraklUpdateShop.setAddress(address);
            miraklUpdateShop.setEmail(miraklCreatedShopReturn.getShopCreated().getContactInformation().getEmail());

            // gets the sales channel code, if multiple found then immutable list should handle
            ImmutableList.Builder<String> channelsBuilder = new ImmutableList.Builder<>();
            for (String channel : miraklCreatedShopReturn.getShopCreated().getChannels()) {
                channelsBuilder.add(channel);
            }
            miraklUpdateShop.setChannels(channelsBuilder.build());
            miraklUpdateShop.setPremiumState(miraklCreatedShopReturn.getShopCreated().getPremiumState());

            // if shop state is either open or close then setSuspend will be false else if enum is already suspend then we will keep it true
            boolean setSuspend;
            MiraklShopState state = miraklCreatedShopReturn.getShopCreated().getState();
            if (state.equals(MiraklShopState.OPEN) || state.equals(MiraklShopState.CLOSE)){
                setSuspend = false;
            } else {
                setSuspend = true;
            }
            miraklUpdateShop.setSuspend(setSuspend);
            miraklUpdateShop.setPaymentBlocked(miraklCreatedShopReturn.getShopCreated().getPaymentDetail().getPaymentBlocked());
        }

        miraklUpdateShop.setAdditionalFieldValues(ImmutableList.of(createAdditionalField("adyen-legal-entity-type", "Individual")));

        MiraklUpdateShopsRequest request = new MiraklUpdateShopsRequest(ImmutableList.of(miraklUpdateShop));
        final MiraklUpdatedShops miraklUpdatedShopsResponse = client.updateShops(request);

        final List<Set<ErrorBean>> errors = miraklUpdatedShopsResponse.getShopReturns().stream()
            .map(MiraklUpdatedShopReturn::getShopError)
            .filter(Objects::nonNull)
            .map(InputWithErrors::getErrors)
            .collect(Collectors.toList());

        Assertions.assertThat(errors.size()).withFailMessage("errors on update: "+ GSON.toJson(errors)).isZero();

        return miraklUpdatedShopsResponse;
    }

    protected MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue createAdditionalField(String code, String value) {
        return new MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue(code, value);
    }
}
