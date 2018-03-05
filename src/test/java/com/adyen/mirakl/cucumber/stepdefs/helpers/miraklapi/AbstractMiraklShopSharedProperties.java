package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;

public abstract class AbstractMiraklShopSharedProperties {

    public MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue createAdditionalField(String code, String value) {
        return new MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue(code, value);
    }
}
