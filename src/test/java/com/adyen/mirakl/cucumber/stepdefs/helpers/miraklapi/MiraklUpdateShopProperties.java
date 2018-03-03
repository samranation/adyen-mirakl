package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.mirakl.client.domain.common.error.ErrorBean;
import com.mirakl.client.domain.common.error.InputWithErrors;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShopAddress;
import com.mirakl.client.mmp.domain.shop.MiraklShopState;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.domain.shop.bank.MiraklPaymentInformation;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdateShop;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShops;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

public class MiraklUpdateShopProperties {

    protected final static Faker FAKER = new Faker(new Locale("en-GB"));
    protected final static Gson GSON = new Gson();

    protected void setMiraklShopTaxId(MiraklCreatedShopReturn miraklCreatedShopReturn) {
        miraklCreatedShopReturn.getShopCreated().getProfessionalInformation().setTaxIdentificationNumber("GB" + RandomStringUtils.randomNumeric(9));
    }

    protected void throwErrorIfShopFailedToUpdate(MiraklUpdatedShops miraklUpdatedShopsResponse) {
        final List<Set<ErrorBean>> errors = miraklUpdatedShopsResponse.getShopReturns().stream()
            .map(MiraklUpdatedShopReturn::getShopError)
            .filter(Objects::nonNull)
            .map(InputWithErrors::getErrors)
            .collect(Collectors.toList());

        Assertions.assertThat(errors.size()).withFailMessage("errors on update: " + GSON.toJson(errors)).isZero();
    }

    protected MiraklIbanBankAccountInformation setNewMiraklIbanOnly(MiraklCreatedShopReturn miraklCreatedShopReturn, List<Map<Object, Object>> rows) {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();
        MiraklPaymentInformation miraklPaymentInformation = miraklCreatedShopReturn.getShopCreated().getPaymentInformation();
        if (miraklPaymentInformation instanceof MiraklIbanBankAccountInformation) {
            paymentInformation.setIban(rows.get(0).get("iban").toString());
            paymentInformation.setBic(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBic());
            paymentInformation.setOwner(miraklPaymentInformation.getOwner());
            paymentInformation.setBankName(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBankName());
        }
        return paymentInformation;
    }

    protected MiraklShopAddress setMiraklShopAddress(MiraklCreatedShopReturn miraklCreatedShopReturn, List<Map<Object, Object>> rows) {
        MiraklShopAddress address = new MiraklShopAddress();
        rows.forEach(row-> {
            address.setCity(row.get("city").toString());
            address.setCivility(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCivility());
            address.setCountry(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCountry());
            address.setFirstname(row.get("firstName").toString());
            address.setLastname(row.get("lastName").toString());
            address.setStreet1(miraklCreatedShopReturn.getShopCreated().getContactInformation().getStreet1());
            address.setZipCode(row.get("postCode").toString());

        });
        return address;
    }

    // Mandatory for shop update
    protected void getMiraklShopChannelAndPremiumStateAndSuspendAndPaymentBlockedStatus(MiraklCreatedShopReturn miraklCreatedShopReturn,
                                                                                        MiraklUpdateShop miraklUpdateShop) {

        getShopNameAndEmail(miraklCreatedShopReturn, miraklUpdateShop);

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
        if (state.equals(MiraklShopState.OPEN) || state.equals(MiraklShopState.CLOSE)) {
            setSuspend = false;
        } else {
            setSuspend = true;
        }
        miraklUpdateShop.setSuspend(setSuspend);
        miraklUpdateShop.setPaymentBlocked(miraklCreatedShopReturn.getShopCreated().getPaymentDetail().getPaymentBlocked());
    }

    protected void getShopNameAndEmail(MiraklCreatedShopReturn miraklCreatedShopReturn, MiraklUpdateShop miraklUpdateShop) {
        miraklUpdateShop.setName(miraklCreatedShopReturn.getShopCreated().getName());
        miraklUpdateShop.setEmail(miraklCreatedShopReturn.getShopCreated().getContactInformation().getEmail());
    }

    // Mandatory for shop update
    protected MiraklShopAddress getMiraklShopAddress(MiraklCreatedShopReturn miraklCreatedShopReturn) {
        MiraklShopAddress address = new MiraklShopAddress();

        address.setCity(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCity());
        address.setCivility(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCivility());
        address.setCountry(miraklCreatedShopReturn.getShopCreated().getContactInformation().getCountry());
        address.setFirstname(miraklCreatedShopReturn.getShopCreated().getContactInformation().getFirstname());
        address.setLastname(miraklCreatedShopReturn.getShopCreated().getContactInformation().getLastname());
        address.setStreet1(miraklCreatedShopReturn.getShopCreated().getContactInformation().getStreet1());
        address.setZipCode(miraklCreatedShopReturn.getShopCreated().getContactInformation().getZipCode());

        return address;
    }

    // Mandatory for shop update
    protected MiraklIbanBankAccountInformation getMiraklIbanBankAccountInformation() {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();

        //update requires bank details for some reason
        paymentInformation.setIban("GB26TEST40051512347366");
        paymentInformation.setBic(FAKER.finance().bic());
        paymentInformation.setOwner(FAKER.name().firstName() + " " + FAKER.name().lastName());
        paymentInformation.setBankName("RBS");

        return paymentInformation;
    }

    // Mandatory for shop update
    protected MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue createAdditionalField(MiraklCreatedShopReturn miraklCreatedShopReturn) {
        String legalValue = miraklCreatedShopReturn.getShopCreated()
            .getAdditionalFieldValues().stream()
            .filter(MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue.class::isInstance)
            .map(MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue.class::cast)
            .filter(x -> "adyen-legal-entity-type".equals(x.getCode()))
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue)
            .findAny().orElse(null);

        return new MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue("adyen-legal-entity-type", legalValue);
    }
}
