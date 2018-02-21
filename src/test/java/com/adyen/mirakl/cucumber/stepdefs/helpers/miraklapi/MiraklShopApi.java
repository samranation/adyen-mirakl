package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableList;
import com.mirakl.client.mmp.domain.shop.MiraklProfessionalInformation;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.domain.shop.create.MiraklCreateShopAddress;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreateShop;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreateShopNewUser;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import com.mirakl.client.mmp.operator.request.shop.MiraklCreateShopsRequest;
import com.mirakl.client.mmp.request.additionalfield.MiraklRequestAdditionalFieldValue;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;

@Service
public class MiraklShopApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public MiraklCreatedShops createNewShop(MiraklMarketplacePlatformOperatorApiClient client) {
        Faker faker = new Faker(new Locale("en-GB"));
        MiraklCreateShop createShop = new MiraklCreateShop();

        MiraklCreateShopAddress address = new MiraklCreateShopAddress();
        address.setCity(faker.address().city());
        address.setCivility("Mr");
        address.setCountry("GBR");
        address.setFirstname(faker.name().firstName());
        address.setLastname(faker.name().lastName());
        address.setStreet1(faker.address().streetAddress());
        address.setZipCode(faker.address().zipCode());
        createShop.setAddress(address);

        createShop.setProfessional(true);
        MiraklProfessionalInformation professionalInformation = new MiraklProfessionalInformation();
        String companyName = faker.company().name();
        professionalInformation.setCorporateName(companyName);
        professionalInformation.setIdentificationNumber(UUID.randomUUID().toString());
        createShop.setProfessionalInformation(professionalInformation);

        String email = ("adyen-mirakl-".concat(UUID.randomUUID().toString()).concat("@mailinator.com"));

        MiraklCreateShopNewUser newUser = new MiraklCreateShopNewUser();
        newUser.setEmail(email);
        createShop.setNewUser(newUser);
        createShop.setEmail(email);

        String shopName = companyName.concat("-").concat(RandomStringUtils.randomAlphanumeric(8)).toLowerCase();
        log.info(String.format("\nShop name to create: [%s]", shopName));
        createShop.setName(shopName);
        MiraklRequestAdditionalFieldValue.MiraklSimpleRequestAdditionalFieldValue additionalFieldValue = new MiraklRequestAdditionalFieldValue
            .MiraklSimpleRequestAdditionalFieldValue();
        additionalFieldValue.setCode("adyen-legal-entity-type");
        additionalFieldValue.setValue("Individual");
        createShop.setAdditionalFieldValues(ImmutableList.of(additionalFieldValue));

        MiraklCreateShopsRequest request = new MiraklCreateShopsRequest(ImmutableList.of(createShop));
        MiraklCreatedShops shops = client.createShops(request);

        MiraklCreatedShopReturn miraklCreatedShopReturn = shops.getShopReturns().stream()
            .findAny().orElseThrow(() -> new IllegalStateException("No Shop found"));

        if (miraklCreatedShopReturn.getShopCreated() == null) {
            throw new IllegalStateException(miraklCreatedShopReturn.getShopError().getErrors().toString());
        }

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

}
