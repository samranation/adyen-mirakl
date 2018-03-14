package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.*;

public class MiraklCreateNewShopSteps extends StepDefsHelper {

    @Given("^a shop has been created in Mirakl for an (.*) with Bank Information$")
    public void aShopHasBeenCreatedInMiraklForAnIndividualWithBankInformation(String legalEntity, DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);

        // create shop
        MiraklCreatedShops shopForIndividualWithBankDetails = miraklShopApi
            .createShopForIndividualWithBankDetails(miraklMarketplacePlatformOperatorApiClient, cucumberTable, legalEntity);

        // get created shop
        MiraklShop createdShop = shopForIndividualWithBankDetails.getShopReturns()
            .stream().map(MiraklCreatedShopReturn::getShopCreated).findFirst().orElse(null);
        cucumberMap.put("createdShop", createdShop);
    }

    @Given("^a new shop has been created in Mirakl for an (.*)$")
    public void aNewShopHasBeenCreatedInMiraklForAnIndividual(String legalEntity, DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklCreatedShops shopForIndividual = miraklShopApi.createShopForIndividual(miraklMarketplacePlatformOperatorApiClient, cucumberTable, legalEntity);

        MiraklShop createdShop = shopForIndividual.getShopReturns()
            .stream().map(MiraklCreatedShopReturn::getShopCreated).findFirst().orElse(null);
        cucumberMap.put("createdShop", createdShop);
    }

    @Given("^a shop has been created in Mirakl for an (.*) with mandatory KYC data$")
    public void aShopHasBeenCreatedInMiraklForAnIndividualWithMandatoryKYCData(String legalEntity, DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklCreatedShops shopForIndividual = miraklShopApi.createShopForIndividualWithBankDetails(miraklMarketplacePlatformOperatorApiClient, cucumberTable, legalEntity);

        MiraklShop createdShop = shopForIndividual.getShopReturns()
            .stream().map(MiraklCreatedShopReturn::getShopCreated).findFirst().orElse(null);
        cucumberMap.put("createdShop", createdShop);
    }

    @When("^a new shop has been created in Mirakl for a (.*)")
    public void aNewShopHasBeenCreatedInMiraklForABusiness(String legalEntity, DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklCreatedShops businessShopWithUbos = miraklShopApi.createBusinessShopWithUbos(miraklMarketplacePlatformOperatorApiClient, cucumberTable, legalEntity);

        MiraklShop createdShop = businessShopWithUbos.getShopReturns()
            .stream().map(MiraklCreatedShopReturn::getShopCreated).findFirst().orElse(null);
        cucumberMap.put("createdShop", createdShop);
    }
}
