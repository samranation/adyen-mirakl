package com.adyen.mirakl.service.util;

import com.adyen.model.Address;
import com.adyen.model.Name;
import com.adyen.model.marketpay.PersonalData;
import com.adyen.model.marketpay.PhoneNumber;
import com.adyen.model.marketpay.ShareholderContact;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ShopUtil {

    private static final String ADYEN_UBO = "adyen-ubo";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String CIVILITY = "civility";
    private static final String EMAIL = "email";
    private static final String COUNTRY = "country";
    private static final String STREET = "street";
    private static final String HOUSE_NUMBER_OR_NAME = "houseNumberOrName";
    private static final String CITY = "city";
    private static final String POSTAL_CODE = "postalCode";
    private static final String STATE_OR_PROVINCE = "stateOrProvince";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    private static final String PHONE_NUMBER = "phoneNumber";

    public final static Map<String, Name.GenderEnum> CIVILITY_TO_GENDER = ImmutableMap.<String, Name.GenderEnum>builder().put("Mr", Name.GenderEnum.MALE)
        .put("Mrs", Name.GenderEnum.FEMALE)
        .put("Miss", Name.GenderEnum.FEMALE)
        .build();

    private ShopUtil() {
    }

    /**
     * Extract shareholder contact data in a adyen format from a mirakl shop
     * @param shop    mirakl shop
     * @param maxUbos number of ubos to be extracted e.g. 4
     * @return share holder contacts to send to adyen
     */
    public static List<ShareholderContact> extractUbos(final MiraklShop shop, Integer maxUbos) {
        Map<String, String> extractedKeysFromMirakl = shop.getAdditionalFieldValues()
            .stream()
            .filter(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::isInstance)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::cast)
            .collect(Collectors.toMap(MiraklAdditionalFieldValue::getCode,
                MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue));

        ImmutableList.Builder<ShareholderContact> builder = ImmutableList.builder();
        generateKeys(maxUbos).forEach((i, keys) -> {
            String firstName = extractedKeysFromMirakl.getOrDefault(keys.get(FIRSTNAME), null);
            String lastName = extractedKeysFromMirakl.getOrDefault(keys.get(LASTNAME), null);
            String civility = extractedKeysFromMirakl.getOrDefault(keys.get(CIVILITY), null);
            String email = extractedKeysFromMirakl.getOrDefault(keys.get(EMAIL), null);
            String country = extractedKeysFromMirakl.getOrDefault(keys.get(COUNTRY), null);
            String street = extractedKeysFromMirakl.getOrDefault(keys.get(STREET), null);
            String houseNumberOrName = extractedKeysFromMirakl.getOrDefault(keys.get(HOUSE_NUMBER_OR_NAME), null);
            String city = extractedKeysFromMirakl.getOrDefault(keys.get(CITY), null);
            String postalCode = extractedKeysFromMirakl.getOrDefault(keys.get(POSTAL_CODE), null);
            String stateOrProvince = extractedKeysFromMirakl.getOrDefault(keys.get(STATE_OR_PROVINCE), null);
            String dateOfBirth = extractedKeysFromMirakl.getOrDefault(keys.get(DATE_OF_BIRTH), null);
            String phoneNumber = extractedKeysFromMirakl.getOrDefault(keys.get(PHONE_NUMBER), null);

            //do nothing if mandatory fields are missing
            if (firstName != null && lastName != null && civility != null && email != null) {

                //mandatory fields
                ShareholderContact shareholderContact = new ShareholderContact();
                Name name = new Name();
                name.setFirstName(firstName);
                name.setLastName(lastName);
                name.setGender(CIVILITY_TO_GENDER.getOrDefault(civility, Name.GenderEnum.UNKNOWN));
                shareholderContact.setName(name);
                shareholderContact.setEmail(email);

                //do not set Phone Number wrapper if the phone number does not exist
                if (phoneNumber != null) {
                    final PhoneNumber phoneNumberWrapper = new PhoneNumber();
                    phoneNumberWrapper.setPhoneNumber(phoneNumber);
                    shareholderContact.setPhoneNumber(phoneNumberWrapper);
                }

                //do not set the address unless at least one address attribute exists
                if (country != null || street != null || houseNumberOrName != null || city != null || postalCode != null || stateOrProvince != null) {
                    final Address address = new Address();
                    Optional.ofNullable(country).ifPresent(address::setCountry);
                    Optional.ofNullable(street).ifPresent(address::setStreet);
                    Optional.ofNullable(houseNumberOrName).ifPresent(address::setHouseNumberOrName);
                    Optional.ofNullable(city).ifPresent(address::setCity);
                    Optional.ofNullable(postalCode).ifPresent(address::setPostalCode);
                    Optional.ofNullable(stateOrProvince).ifPresent(address::setStateOrProvince);
                    shareholderContact.setAddress(address);
                }

                //do not set the dob wrapper if the dob does not exist
                if (dateOfBirth != null) {
                    final PersonalData personalData = new PersonalData();
                    personalData.setDateOfBirth(dateOfBirth);
                    shareholderContact.setPersonalData(personalData);
                }

                builder.add(shareholderContact);
            }
        });
        return builder.build();
    }

    private static Map<Integer, Map<String, String>> generateKeys(Integer maxUbos) {
        return IntStream.rangeClosed(1, maxUbos).mapToObj(i -> {
            final Map<Integer, Map<String, String>> grouped = new HashMap<>();
            grouped.put(i, new ImmutableMap.Builder<String, String>()
                .put(FIRSTNAME, ADYEN_UBO + String.valueOf(i) + "-firstname")
                .put(LASTNAME, ADYEN_UBO + String.valueOf(i) + "-lastname")
                .put(CIVILITY, ADYEN_UBO + String.valueOf(i) + "-civility")
                .put(EMAIL, ADYEN_UBO + String.valueOf(i) + "-email")
                .put(COUNTRY, ADYEN_UBO + String.valueOf(i) + "-country")
                .put(STREET, ADYEN_UBO + String.valueOf(i) + "-street")
                .put(HOUSE_NUMBER_OR_NAME, ADYEN_UBO + String.valueOf(i) + "-houseNumberOrName")
                .put(CITY, ADYEN_UBO + String.valueOf(i) + "-city")
                .put(POSTAL_CODE, ADYEN_UBO + String.valueOf(i) + "-postalCode")
                .put(STATE_OR_PROVINCE, ADYEN_UBO + String.valueOf(i) + "-stateOrProvince")
                .put(DATE_OF_BIRTH, ADYEN_UBO + String.valueOf(i) + "-dateOfBirth")
                .put(PHONE_NUMBER, ADYEN_UBO + String.valueOf(i) + "-phoneNumber")
                .build());
            return grouped;
        }).reduce((x, y) -> {
            x.put(y.entrySet().iterator().next().getKey(), y.entrySet().iterator().next().getValue());
            return x;
        }).orElseThrow(() -> new IllegalStateException("UBOs must exist, number found: " + maxUbos));
    }
}
