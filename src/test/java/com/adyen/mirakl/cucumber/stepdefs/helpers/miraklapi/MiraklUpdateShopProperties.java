package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.mirakl.client.domain.common.error.ErrorBean;
import com.mirakl.client.domain.common.error.InputWithErrors;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShopAddress;
import com.mirakl.client.mmp.domain.shop.MiraklShopState;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.domain.shop.bank.MiraklPaymentInformation;
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

    protected void updateMiraklShopTaxId(MiraklShop miraklShop) {
        miraklShop.getProfessionalInformation().setTaxIdentificationNumber("GB" + RandomStringUtils.randomNumeric(9));
    }

    protected void throwErrorIfShopFailedToUpdate(MiraklUpdatedShops miraklUpdatedShopsResponse) {
        final List<Set<ErrorBean>> errors = miraklUpdatedShopsResponse.getShopReturns().stream()
            .map(MiraklUpdatedShopReturn::getShopError)
            .filter(Objects::nonNull)
            .map(InputWithErrors::getErrors)
            .collect(Collectors.toList());

        Assertions.assertThat(errors.size()).withFailMessage("errors on update: " + GSON.toJson(errors)).isZero();
    }

    protected MiraklIbanBankAccountInformation updateNewMiraklIbanOnly(MiraklShop miraklShop, List<Map<Object, Object>> rows) {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();
        MiraklPaymentInformation miraklPaymentInformation = miraklShop.getPaymentInformation();
        if (miraklPaymentInformation instanceof MiraklIbanBankAccountInformation) {
            paymentInformation.setIban(rows.get(0).get("iban").toString());
            paymentInformation.setBic(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBic());
            paymentInformation.setOwner(miraklPaymentInformation.getOwner());
            paymentInformation.setBankName(((MiraklIbanBankAccountInformation) miraklPaymentInformation).getBankName());
        }
        return paymentInformation;
    }

    protected MiraklShopAddress updateMiraklShopAddress(MiraklShop miraklShop, List<Map<Object, Object>> rows) {
        MiraklShopAddress address = new MiraklShopAddress();
        rows.forEach(row-> {
            address.setCity(row.get("city").toString());
            address.setCivility(miraklShop.getContactInformation().getCivility());
            address.setCountry(miraklShop.getContactInformation().getCountry());
            address.setFirstname(row.get("firstName").toString());
            address.setLastname(row.get("lastName").toString());
            address.setStreet1(miraklShop.getContactInformation().getStreet1());
            address.setZipCode(row.get("postCode").toString());

        });
        return address;
    }

    // Mandatory for shop update
    protected void populateMiraklShopPremiumSuspendAndPaymentBlockedStatus(MiraklShop miraklShop,
                                                                           MiraklUpdateShop miraklUpdateShop) {

        // will keep setSuspend false unless returned enum = SUSPEND
        boolean setSuspend;
        MiraklShopState state = miraklShop.getState();
        setSuspend = state.equals(MiraklShopState.SUSPENDED);
        miraklUpdateShop.setSuspend(setSuspend);

        miraklUpdateShop.setPaymentBlocked(miraklShop.getPaymentDetail().getPaymentBlocked());
        miraklUpdateShop.setPremiumState(miraklShop.getPremiumState());
    }

    protected void populateMiraklChannel(MiraklShop miraklShop, MiraklUpdateShop miraklUpdateShop) {
        // gets the sales channel code, if multiple found then immutable list should handle
        ImmutableList.Builder<String> channelsBuilder = new ImmutableList.Builder<>();
        for (String channel : miraklShop.getChannels()) {
            channelsBuilder.add(channel);
        }
        miraklUpdateShop.setChannels(channelsBuilder.build());
    }

    // Mandatory for shop update
    protected void populateShopNameAndEmail(MiraklShop miraklShop, MiraklUpdateShop miraklUpdateShop) {
        miraklUpdateShop.setName(miraklShop.getName());
        miraklUpdateShop.setEmail(miraklShop.getContactInformation().getEmail());
    }

    // Mandatory for shop update
    protected MiraklShopAddress populateMiraklShopAddress(MiraklShop miraklShop) {
        MiraklShopAddress address = new MiraklShopAddress();

        address.setCity(miraklShop.getContactInformation().getCity());
        address.setCivility(miraklShop.getContactInformation().getCivility());
        address.setCountry(miraklShop.getContactInformation().getCountry());
        address.setFirstname(miraklShop.getContactInformation().getFirstname());
        address.setLastname(miraklShop.getContactInformation().getLastname());
        address.setStreet1(miraklShop.getContactInformation().getStreet1());
        address.setZipCode(miraklShop.getContactInformation().getZipCode());

        return address;
    }

    // Mandatory for shop update
    protected MiraklIbanBankAccountInformation populateMiraklIbanBankAccountInformation() {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();

        //update requires bank details for some reason
        paymentInformation.setIban("GB26TEST40051512347366");
        paymentInformation.setBic(FAKER.finance().bic());
        paymentInformation.setOwner(FAKER.name().firstName() + " " + FAKER.name().lastName());
        paymentInformation.setBankName("RBS");

        return paymentInformation;
    }

    // Mandatory for shop update
    protected MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue createAdditionalField(MiraklShop miraklShop) {
        String legalValue = miraklShop
            .getAdditionalFieldValues().stream()
            .filter(MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue.class::isInstance)
            .map(MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue.class::cast)
            .filter(x -> "adyen-legal-entity-type".equals(x.getCode()))
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue)
            .findAny().orElse(null);

        return new MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue("adyen-legal-entity-type", legalValue);
    }
}
