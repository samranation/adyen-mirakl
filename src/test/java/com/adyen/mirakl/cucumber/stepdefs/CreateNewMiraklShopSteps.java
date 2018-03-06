package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklShopApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;

public class CreateNewMiraklShopSteps extends StepDefsHelper {

    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Resource
    private MiraklShopApi miraklShopApi;

    @Given("^a shop has been created in Mirakl for an (.*) with mandatory KYC data$")
    public void aShopHasBeenCreatedInMiraklForAnIndividualWithMandatoryKYCData(String legalEntity, DataTable table) throws Throwable {
        final List<Map<Object, Object>> rows = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklCreatedShops shopForIndividual = miraklShopApi.createShopForIndividualWithFullKYCData(miraklMarketplacePlatformOperatorApiClient, rows, legalEntity);
        cucumberMap.put("createdShops", shopForIndividual);
    }
}
