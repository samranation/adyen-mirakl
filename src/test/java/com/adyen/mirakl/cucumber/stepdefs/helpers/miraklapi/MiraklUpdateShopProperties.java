package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.mirakl.client.domain.common.error.ErrorBean;
import com.mirakl.client.domain.common.error.InputWithErrors;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklProfessionalInformation;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShopAddress;
import com.mirakl.client.mmp.domain.shop.MiraklShopState;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.domain.shop.bank.MiraklPaymentInformation;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdateShop;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.update.MiraklUpdatedShops;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;
import com.mirakl.client.mmp.request.common.document.MiraklUploadDocument;
import com.mirakl.client.mmp.request.shop.document.MiraklUploadShopDocumentsRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MiraklUpdateShopProperties extends AbstractMiraklShopSharedProperties {

    protected MiraklProfessionalInformation updateMiraklShopTaxId(MiraklShop miraklShop) {
        MiraklProfessionalInformation miraklProfessionalInformation = new MiraklProfessionalInformation();
        miraklProfessionalInformation.setTaxIdentificationNumber("GB" + RandomStringUtils.randomNumeric(9));
        miraklProfessionalInformation.setIdentificationNumber(miraklShop.getProfessionalInformation().getIdentificationNumber());
        miraklProfessionalInformation.setCorporateName(miraklShop.getProfessionalInformation().getCorporateName());
        return miraklProfessionalInformation;
    }

    protected MiraklUploadShopDocumentsRequest uploadMiraklShopWithBankStatement(String shopId) {
        ImmutableList.Builder<MiraklUploadDocument> docUploadRequestBuilder = new ImmutableList.Builder<>();

        URL url = Resources.getResource("fileuploads/BankStatement.jpg");

        MiraklUploadDocument element = new MiraklUploadDocument();
        element.setFile(new File(url.getPath()));
        element.setFileName("BankStatement.jpg");
        element.setTypeCode("adyen-bankproof");

        docUploadRequestBuilder.add(element);

        return miraklUploadShopDocumentsRequest(shopId, docUploadRequestBuilder.build());
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

    protected MiraklShopAddress updateMiraklShopAddress(MiraklShop miraklShop, Map<Object, Object> row) {
        MiraklShopAddress address = new MiraklShopAddress();

        address.setCity(row.get("city").toString());
        address.setCivility(miraklShop.getContactInformation().getCivility());
        address.setCountry(miraklShop.getContactInformation().getCountry());
        address.setFirstname(row.get("firstName").toString());
        address.setLastname(row.get("lastName").toString());
        address.setStreet1(miraklShop.getContactInformation().getStreet1());
        address.setZipCode(row.get("postCode").toString());

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
        miraklUpdateShop.setProfessional(miraklShop.isProfessional());
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
    protected MiraklIbanBankAccountInformation populateMiraklIbanBankAccountInformation(MiraklShop miraklShop) {
        MiraklIbanBankAccountInformation paymentInformation = new MiraklIbanBankAccountInformation();

        //update requires bank details for some reason
        if (miraklShop.getPaymentInformation() != null) {
            if (miraklShop.getPaymentInformation() instanceof MiraklIbanBankAccountInformation) {
                MiraklIbanBankAccountInformation ibanBankAccountInformation = (MiraklIbanBankAccountInformation) miraklShop.getPaymentInformation();
                paymentInformation.setIban(ibanBankAccountInformation.getIban());
                paymentInformation.setBic(ibanBankAccountInformation.getBic());
                paymentInformation.setOwner(ibanBankAccountInformation.getOwner());
                paymentInformation.setBankName(ibanBankAccountInformation.getBankName());
                paymentInformation.setBankCity(ibanBankAccountInformation.getBankCity());
            }
        } else {
            paymentInformation.setIban("GB26TEST40051512347366");
            paymentInformation.setBic(FAKER.finance().bic());
            paymentInformation.setOwner(FAKER.name().firstName() + " " + FAKER.name().lastName());
            paymentInformation.setBankName("RBS");
            paymentInformation.setBankCity("PASSED");
        }
        return paymentInformation;
    }

    protected void populateMiraklAdditionalFields(MiraklUpdateShop miraklUpdateShop, MiraklShop miraklShop, Map<String, String> fieldsToUpdate) {
        final List<MiraklAdditionalFieldValue> addFields = new LinkedList<>(miraklShop.getAdditionalFieldValues());

        for (String fieldCode : fieldsToUpdate.keySet()) {
            MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue result = addFields.stream()
                .filter(x -> fieldCode.equals(x.getCode()))
                .filter(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::isInstance)
                .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::cast)
                .findFirst().orElseThrow(() -> new IllegalStateException("Field: '" + fieldCode + "' was not found."));

            result.setValue(fieldsToUpdate.get(fieldCode));
        }

        final List<MiraklRequestAdditionalFieldValue> updatedAddFields = new LinkedList<>();
        for (MiraklAdditionalFieldValue field : addFields) {
            if (field instanceof MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithMultipleValues) {
                updatedAddFields.add(new MiraklRequestAdditionalFieldValue.MiraklMultipleRequestAdditionalFieldValue(field.getCode(),
                    ((MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithMultipleValues) field).getValues()));
            } else if (field instanceof MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue) {
                updatedAddFields.add(new MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue(field.getCode(),
                    ((MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue) field).getValue()));
            } else {
                Assertions.fail("unexpected additional field type {0} ", field.getClass());
            }
        }
        miraklUpdateShop.setAdditionalFieldValues(updatedAddFields);
    }

    protected void throwErrorIfShopFailedToUpdate(MiraklUpdatedShops miraklUpdatedShopsResponse) {
        final List<Set<ErrorBean>> errors = miraklUpdatedShopsResponse.getShopReturns().stream()
            .map(MiraklUpdatedShopReturn::getShopError)
            .filter(Objects::nonNull)
            .map(InputWithErrors::getErrors)
            .collect(Collectors.toList());

        Assertions.assertThat(errors.size()).withFailMessage("errors on update: " + GSON.toJson(errors)).isZero();
    }
}
