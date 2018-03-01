package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.StartUpTestingHook;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklShopApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.restassured.RestAssuredAdyenApi;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class AccountHolderVerificationSteps {
    @Resource
    private MiraklShopApi miraklShopApi;
    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Resource
    private StartUpTestingHook startUpTestingHook;
    private MiraklCreatedShops createdShops;
    private String shopId;
    @Resource
    private RestAssuredAdyenApi restAssuredAdyenApi;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private boolean createShareHolderDate = false;


    @Given("^a shop has been created in Mirakl with a corresponding account holder in Adyen with the following data$")
    public void aShopHasBeenCreatedInMiraklWithACorrespondingAccountHolderInAdyenWithTheFollowingData(DataTable table) throws Throwable {
        List<Map<Object, Object>> tableMap = table.getTableConverter().toMaps(table, String.class, String.class);
        tableMap.forEach(map -> createdShops = miraklShopApi.createNewShop(miraklMarketplacePlatformOperatorApiClient, map, createShareHolderDate));
    }

    @Then("^the (.*) notification is sent by Adyen comprising of (.*) and (.*)")
    public void theACCOUNT_HOLDER_VERIFICATIONNotificationIsSentByAdyenComprisingOfBANK_ACCOUNT_VERIFICATIONAndPASSED(String notification,
                                                                                                                      String verificationType,
                                                                                                                      String verificationStatus) throws Throwable {

        shopId = createdShops.getShopReturns().iterator().next().getShopCreated().getId();

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {

            Map<String, Object> adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), shopId, notification, verificationType);

            Assertions.assertThat(adyenNotificationBody).isNotNull();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationStatus").toString()).isEqualTo(verificationStatus);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationType").toString()).isEqualTo(verificationType);
        });
    }
}
