package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.java.en.When;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;

public class UpdateMiraklShopsSteps extends StepDefsHelper {

    @When("^the seller uploads a Bank Statement in Mirakl$")
    public void theSellerUploadsABankStatementInMirakl() {
        MiraklCreatedShops createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        String shopId = retrieveShopIdFromCreatedShop(createdShops);
        miraklUpdateShopApi.uploadBankStatementToExistingShop(shopId, miraklMarketplacePlatformOperatorApiClient);
    }
}
