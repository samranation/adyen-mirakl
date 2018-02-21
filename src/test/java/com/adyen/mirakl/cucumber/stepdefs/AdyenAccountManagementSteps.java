package com.adyen.mirakl.cucumber.stepdefs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.assertj.core.api.Assertions;
import org.springframework.util.CollectionUtils;
import com.adyen.mirakl.cucumber.stepdefs.helpers.Hooks.StartUpCucumberHook;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklShopApi;
import com.adyen.mirakl.service.ShopService;
import com.adyen.model.marketpay.GetAccountHolderRequest;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.service.Account;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import static org.awaitility.Awaitility.await;

public class AdyenAccountManagementSteps {

    @Resource
    private StartUpCucumberHook startUpCucumberHook;
    @Resource
    private Account adyenAccountService;
    @Resource
    private ShopService shopService;
    @Resource
    private MiraklShopApi miraklShopApi;
    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    private MiraklCreatedShops createdShops;
    private String notification;

    @Given("^a new shop has been created in Mirakl$")
    public void aNewShopHasBeenCreatedInMirakl() throws Throwable {
        createdShops = miraklShopApi.createNewShop(miraklMarketplacePlatformOperatorApiClient);
    }

    @Then("^we process the data and push to Adyen$")
    public void adyenWillProcessTheData() throws Throwable {
        shopService.retrieveUpdatedShops();
    }

    @And("^an AccountHolder will be created in Adyen with status Active$")
    public void anAccountHolderWillBeCreatedInAdyenWithStatusActive() throws Throwable {
        GetAccountHolderRequest accountHolderRequest = new GetAccountHolderRequest();
        String email = createdShops.getShopReturns().iterator().next().getShopCreated().getContactInformation().getEmail();
        MiraklShop miraklShop = miraklShopApi.filterMiraklShopsByEmailAndReturnShop(miraklMarketplacePlatformOperatorApiClient, email);
        accountHolderRequest.setAccountHolderCode(miraklShop.getId());

        GetAccountHolderResponse accountHolderResponse = adyenAccountService.getAccountHolder(accountHolderRequest);
        Assertions.assertThat(accountHolderResponse.getAccountHolderStatus().getStatus().toString()).isEqualTo("Active");
    }

    @And("^a notification will be sent pertaining to (.*)$")
    public void aNotificationWillBeSentPertainingToACCOUNT_HOLDER_CREATED(String notification) throws Throwable {
        this.notification = notification;
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(()->{
            ResponseBody body = RestAssured.get(startUpCucumberHook.getBaseRequestBinUrlPath()).getBody();
            List<String> check = body.jsonPath().get("body");
            if(!CollectionUtils.isEmpty(check)){
                String json = check.iterator().next();
                Map<String, Object> mapResult = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {}.getType());
                Assertions.assertThat(mapResult.get("eventType")).isEqualTo(this.notification);
            }
        });
    }
}
