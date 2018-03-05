package com.adyen.mirakl.service.util;

import com.adyen.model.Name;
import com.adyen.model.marketpay.ShareholderContact;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ShopUtil {

    private static final String ADYEN_UBO = "adyen-ubo";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String CIVILITY = "civility";
    private static final String EMAIL = "email";

    public static Map<String, Name.GenderEnum> CIVILITY_TO_GENDER = ImmutableMap.<String, Name.GenderEnum>builder().put("Mr", Name.GenderEnum.MALE)
        .put("Mrs", Name.GenderEnum.FEMALE)
        .put("Miss", Name.GenderEnum.FEMALE)
        .build();

    public static List<ShareholderContact> extractUbos(final MiraklShop shop, Integer maxUbos) {
        Map<String, String> extractedKeysFromMirakl = shop.getAdditionalFieldValues()
            .stream()
            .filter(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::isInstance)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::cast)
            .collect(Collectors.toMap(MiraklAdditionalFieldValue::getCode,
                MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue));

        ImmutableList.Builder<ShareholderContact> builder = ImmutableList.builder();
        generateKeys(maxUbos).forEach((i, keys) -> {
            String firstName = extractedKeysFromMirakl.getOrDefault(keys.get(FIRSTNAME), "");
            String lastName = extractedKeysFromMirakl.getOrDefault(keys.get(LASTNAME), "");
            String civility = extractedKeysFromMirakl.getOrDefault(keys.get(CIVILITY), "");
            String email = extractedKeysFromMirakl.getOrDefault(keys.get(EMAIL), "");

            if (ImmutableList.of(firstName, lastName, civility, email).stream().noneMatch(StringUtils::isBlank)) {
                ShareholderContact shareholderContact = new ShareholderContact();
                Name name = new Name();
                name.setFirstName(firstName);
                name.setLastName(lastName);
                name.setGender(CIVILITY_TO_GENDER.getOrDefault(civility, Name.GenderEnum.UNKNOWN));
                shareholderContact.setName(name);
                shareholderContact.setEmail(email);
                builder.add(shareholderContact);
            }
        });
        return builder.build();
    }

    private static Map<Integer, Map<String, String>> generateKeys(Integer maxUbos) {
        return IntStream.rangeClosed(1, maxUbos).mapToObj(i -> {
            final Map<Integer, Map<String, String>> grouped = new HashMap<>();
            grouped.put(i,
                ImmutableMap.of(FIRSTNAME,
                    ADYEN_UBO + String.valueOf(i) + "-firstname",
                    LASTNAME,
                    ADYEN_UBO + String.valueOf(i) + "-lastname",
                    CIVILITY,
                    ADYEN_UBO + String.valueOf(i) + "-civility",
                    EMAIL,
                    ADYEN_UBO + String.valueOf(i) + "-email"));
            return grouped;
        }).reduce((x, y) -> {
            x.put(y.entrySet().iterator().next().getKey(), y.entrySet().iterator().next().getValue());
            return x;
        }).orElseThrow(() -> new IllegalStateException("UBOs must exist, number found: " + maxUbos));
    }
}
