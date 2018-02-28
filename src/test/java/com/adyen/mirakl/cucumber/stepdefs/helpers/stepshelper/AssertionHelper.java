package com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;

@Service
public class AssertionHelper {

    private static final Map<String, String> CIVILITY_TO_GENDER = ImmutableMap.<String, String>builder().put("Mr", "MALE")
                                                                                                        .put("Mrs", "FEMALE")
                                                                                                        .put("Miss", "FEMALE").build();

    public ImmutableList.Builder<String> adyenAccountDataBuilder(Map<String, Object> mappedAdyenNotificationResponse) {
        ImmutableList.Builder<String> adyenShopData = new ImmutableList.Builder<>();

        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("['accountHolderCode']").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("['accountHolderDetails']['individualDetails']['name']['firstName']").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("['accountHolderDetails']['individualDetails']['name']['lastName']").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("['accountHolderDetails']['individualDetails']['name']['gender']").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("['accountHolderDetails']['email']").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("['legalEntity']").toString());
        return adyenShopData;
    }

    public ImmutableList.Builder<String> adyenShareHolderAccountDataBuilder(Map<String, Object> mappedAdyenNotificationResponse) {
        ImmutableList.Builder<String> adyenShopData = new ImmutableList.Builder<>();

        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("accountHolderCode").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("accountHolderDetails.businessDetails.shareholders[0]ShareholderContact.name.firstName").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("accountHolderDetails.businessDetails.shareholders[0]ShareholderContact.name.lastName").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("accountHolderDetails.businessDetails.shareholders[0]ShareholderContact.email").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("legalEntity").toString());
        adyenShopData.add(JsonPath.parse(mappedAdyenNotificationResponse.get("content")).read("accountHolderDetails.businessDetails.shareholders[0]ShareholderContact.name.gender").toString());

        return adyenShopData;
    }

    public ImmutableList.Builder<String> miraklShopShareHolderDataBuilder(MiraklShop miraklShop){
        ImmutableList.Builder<String> miraklShopData = new ImmutableList.Builder<>();

        miraklShopData.add(miraklShop.getId());

        List<String> shopAdditionalFields = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            shopAdditionalFields.add("adyen-ubo"+i+"-firstname");
            shopAdditionalFields.add("adyen-ubo"+i+"-lastname");
            shopAdditionalFields.add("adyen-ubo"+i+"-email");
            shopAdditionalFields.add("adyen-legal-entity-type");
            shopAdditionalFields.add("adyen-ubo"+i+"-civility");

            for (String field : shopAdditionalFields) {
                String fieldValue = miraklShop.getAdditionalFieldValues().stream()
                    .filter(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue.class::isInstance)
                    .map(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue.class::cast)
                    .filter(x -> field.equals(x.getCode()))
                    .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue)
                    .findAny()
                    .orElse(null);
                if (field.contains("civility")){
                    miraklShopData.add(CIVILITY_TO_GENDER.get(fieldValue));
                }
                miraklShopData.add(fieldValue);
            }
        }

        return miraklShopData;
    }

    public ImmutableList.Builder<String> miraklShopDataBuilder(String email, MiraklShop miraklShop){
        ImmutableList.Builder<String> miraklShopData = new ImmutableList.Builder<>();

        miraklShopData.add(miraklShop.getId());
        miraklShopData.add(miraklShop.getContactInformation().getFirstname());
        miraklShopData.add(miraklShop.getContactInformation().getLastname());
        miraklShopData.add(CIVILITY_TO_GENDER.get(miraklShop.getContactInformation().getCivility()));
        miraklShopData.add(email);
        miraklShopData.add(miraklShop.getAdditionalFieldValues()
                                     .stream()
                                     .findAny()
                                     .filter(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue.class::isInstance)
                                     .map(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue.class::cast)
                                     .map(MiraklAdditionalFieldValue.MiraklStringAdditionalFieldValue::getValue)
                                     .orElse(null));
        return miraklShopData;
    }
}
