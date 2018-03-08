package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;
import com.mirakl.client.mmp.request.common.document.MiraklUploadDocument;
import com.mirakl.client.mmp.request.shop.document.MiraklUploadShopDocumentsRequest;

import java.util.Locale;

public abstract class AbstractMiraklShopSharedProperties {

    protected final static Faker FAKER = new Faker(new Locale("en-GB"));
    protected final static Gson GSON = new Gson();

    protected MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue createAdditionalField(String code, String value) {
        return new MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue(code, value);
    }

    protected MiraklUploadShopDocumentsRequest miraklUploadShopDocumentsRequest(String shopId, ImmutableList<MiraklUploadDocument> miraklUploadDocuments) {
        return new MiraklUploadShopDocumentsRequest(shopId, miraklUploadDocuments);
    }
}
