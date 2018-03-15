package com.adyen.mirakl.service.util;

import com.adyen.mirakl.web.rest.util.HeaderUtil;
import com.adyen.model.Address;
import com.adyen.model.Name;
import com.adyen.model.marketpay.PersonalData;
import com.adyen.model.marketpay.PhoneNumber;
import com.adyen.model.marketpay.ShareholderContact;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ShopUtil {

    private static final Logger log = LoggerFactory.getLogger(ShopUtil.class);

    private static final String ADYEN_UBO = "adyen-ubo";

    private static final String CIVILITY = "civility";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String EMAIL = "email";
    private static final String DATE_OF_BIRTH = "dob";
    private static final String NATIONALITY = "nationality";
    private static final String ID_NUMBER = "idnumber";
    private static final String HOUSE_NUMBER_OR_NAME = "housenumber";
    private static final String STREET = "streetname";
    private static final String CITY = "city";
    private static final String POSTAL_CODE = "zip";
    private static final String COUNTRY = "country";
    private static final String PHONE_COUNTRY_CODE = "phonecountry";
    private static final String PHONE_TYPE = "phonetype";
    private static final String PHONE_NUMBER = "phonenumber";

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
        generateKeys(maxUbos).forEach((uboNumber, uboKeys) -> {
            String civility = extractedKeysFromMirakl.getOrDefault(uboKeys.get(CIVILITY), null);
            String firstName = extractedKeysFromMirakl.getOrDefault(uboKeys.get(FIRSTNAME), null);
            String lastName = extractedKeysFromMirakl.getOrDefault(uboKeys.get(LASTNAME), null);
            String email = extractedKeysFromMirakl.getOrDefault(uboKeys.get(EMAIL), null);
            String dateOfBirth = extractedKeysFromMirakl.getOrDefault(uboKeys.get(DATE_OF_BIRTH), null);
            String nationality = extractedKeysFromMirakl.getOrDefault(uboKeys.get(NATIONALITY), null);
            String idNumber = extractedKeysFromMirakl.getOrDefault(uboKeys.get(ID_NUMBER), null);
            String houseNumberOrName = extractedKeysFromMirakl.getOrDefault(uboKeys.get(HOUSE_NUMBER_OR_NAME), null);
            String street = extractedKeysFromMirakl.getOrDefault(uboKeys.get(STREET), null);
            String city = extractedKeysFromMirakl.getOrDefault(uboKeys.get(CITY), null);
            String postalCode = extractedKeysFromMirakl.getOrDefault(uboKeys.get(POSTAL_CODE), null);
            String country = extractedKeysFromMirakl.getOrDefault(uboKeys.get(COUNTRY), null);
            String phoneType = extractedKeysFromMirakl.getOrDefault(uboKeys.get(PHONE_TYPE), null);
            String phoneNumber = extractedKeysFromMirakl.getOrDefault(uboKeys.get(PHONE_NUMBER), null);

            //do nothing if mandatory fields are missing
            if (firstName != null && lastName != null && civility != null && email != null) {
                ShareholderContact shareholderContact = new ShareholderContact();

                //mandatory fields
                Name name = new Name();
                name.setGender(CIVILITY_TO_GENDER.getOrDefault(civility, Name.GenderEnum.UNKNOWN));
                name.setFirstName(firstName);
                name.setLastName(lastName);
                shareholderContact.setName(name);
                shareholderContact.setEmail(email);

                if (dateOfBirth != null || nationality != null || idNumber != null) {
                    final PersonalData personalData = new PersonalData();
                    Optional.ofNullable(dateOfBirth).ifPresent(personalData::setDateOfBirth);
                    Optional.ofNullable(nationality).ifPresent(personalData::setNationality);
                    Optional.ofNullable(idNumber).ifPresent(personalData::setIdNumber);
                    shareholderContact.setPersonalData(personalData);
                }else{
                    log.warn("Unable to populate any personal data for share holder {}", uboNumber);
                }

                if (country != null || street != null || houseNumberOrName != null || city != null || postalCode != null) {
                    final Address address = new Address();
                    Optional.ofNullable(houseNumberOrName).ifPresent(address::setHouseNumberOrName);
                    Optional.ofNullable(street).ifPresent(address::setStreet);
                    Optional.ofNullable(city).ifPresent(address::setCity);
                    Optional.ofNullable(postalCode).ifPresent(address::setPostalCode);
                    Optional.ofNullable(country).ifPresent(address::setCountry);
                    shareholderContact.setAddress(address);
                }else{
                    log.warn("Unable to populate any address data for share holder {}", uboNumber);
                }

                if (phoneNumber != null || phoneType != null) {
                    final PhoneNumber phoneNumberWrapper = new PhoneNumber();
                    Optional.ofNullable(phoneNumber).ifPresent(phoneNumberWrapper::setPhoneNumber);
                    Optional.ofNullable(phoneType).ifPresent(x -> phoneNumberWrapper.setPhoneType(PhoneNumber.PhoneTypeEnum.valueOf(x)));
                    shareholderContact.setPhoneNumber(phoneNumberWrapper);
                }else{
                    log.warn("Unable to populate any phone data for share holder {}", uboNumber);
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
                .put(CIVILITY, ADYEN_UBO + String.valueOf(i) + "-civility")
                .put(FIRSTNAME, ADYEN_UBO + String.valueOf(i) + "-firstname")
                .put(LASTNAME, ADYEN_UBO + String.valueOf(i) + "-lastname")
                .put(EMAIL, ADYEN_UBO + String.valueOf(i) + "-email")
                .put(DATE_OF_BIRTH, ADYEN_UBO + String.valueOf(i) + "-dob")
                .put(NATIONALITY, ADYEN_UBO + String.valueOf(i) + "-nationality")
                .put(ID_NUMBER, ADYEN_UBO + String.valueOf(i) + "-idnumber")
                .put(HOUSE_NUMBER_OR_NAME, ADYEN_UBO + String.valueOf(i) + "-housenumber")
                .put(STREET, ADYEN_UBO + String.valueOf(i) + "-streetname")
                .put(CITY, ADYEN_UBO + String.valueOf(i) + "-city")
                .put(POSTAL_CODE, ADYEN_UBO + String.valueOf(i) + "-zip")
                .put(COUNTRY, ADYEN_UBO + String.valueOf(i) + "-country")
                .put(PHONE_COUNTRY_CODE, ADYEN_UBO + String.valueOf(i) + "-phonecountry")
                .put(PHONE_TYPE, ADYEN_UBO + String.valueOf(i) + "-phonetype")
                .put(PHONE_NUMBER, ADYEN_UBO + String.valueOf(i) + "-phonenumber")
                .build());
            return grouped;
        }).reduce((x, y) -> {
            x.put(y.entrySet().iterator().next().getKey(), y.entrySet().iterator().next().getValue());
            return x;
        }).orElseThrow(() -> new IllegalStateException("UBOs must exist, number found: " + maxUbos));
    }
}
