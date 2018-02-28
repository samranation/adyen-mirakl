package com.adyen.mirakl.cucumber.stepdefs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.StartUpCucumberHook;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklShopApi;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.awaitility.Awaitility.await;

public class AccountHolderVerificationSteps {
    @Resource
    private MiraklShopApi miraklShopApi;
    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Resource
    private StartUpCucumberHook startUpCucumberHook;
    private MiraklCreatedShops createdShops;
    private String verificationStatus;
    private String shopId;

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
        this.verificationStatus = verificationStatus;

        await().atMost(Duration.TEN_SECONDS).untilAsserted(() -> {
            Response response = RestAssured.get(startUpCucumberHook.getBaseRequestBinUrlPath());
            List<String> body = response.getBody().jsonPath().get("body");
            Map<String, Object> mapResult = new HashMap<>();
            if (! CollectionUtils.isEmpty(body)) {
                for (String list : body) {
                    mapResult.putAll(new Gson().fromJson(list, new TypeToken<HashMap<String, Object>>() {
                    }.getType()));
                    shopId = createdShops.getShopReturns().iterator().next().getShopCreated().getId();


                    Map contentMap = (Map) mapResult.get("content");
                    if (contentMap.get("accountHolderCode").equals(shopId) && contentMap.get("verificationType").equals(verificationType)) {
                        Assertions.assertThat(contentMap.get("verificationStatus")).isEqualTo(this.verificationStatus);
                        break;
                    }
                }
            }
        });
    }
}
