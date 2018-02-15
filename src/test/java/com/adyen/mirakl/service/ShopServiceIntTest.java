package com.adyen.mirakl.service;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import com.adyen.mirakl.AdyenMiraklConnectorApp;
import com.adyen.mirakl.config.MiraklFrontApiClientFactory;
import com.adyen.mirakl.startup.StartupValidator;
import com.adyen.model.marketpay.CreateAccountHolderRequest;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import static org.junit.Assert.assertEquals;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
@Transactional
public class ShopServiceIntTest {

    @Autowired
    private MiraklFrontApiClientFactory miraklFrontApiClientFactory;

    @Autowired
    private ShopService shopService;

    @Test
    public void testCreateAHRequest() {
        MiraklShop shop = new MiraklShop();
        List<MiraklAdditionalFieldValue> additionalFields = new ArrayList<>();
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalField = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalField.setCode(String.valueOf(StartupValidator.CustomMiraklFields.ADYEN_LEGAL_ENTITY_TYPE));
        additionalField.setValue("BUSINESS");

        additionalFields.add(additionalField);
        shop.setAdditionalFieldValues(additionalFields);
        CreateAccountHolderRequest request = shopService.createAccountHolderRequestFromShop(shop);
        assertEquals(CreateAccountHolderRequest.LegalEntityEnum.BUSINESS, request.getLegalEntity());
    }
}
