package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;

public class MiraklUpdateShopsSteps extends StepDefsHelper {

    @When("^the seller uploads a Bank Statement in Mirakl$")
    public void theSellerUploadsABankStatementInMirakl() {
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");
        miraklUpdateShopApi
            .uploadBankStatementToExistingShop(createdShop.getId(), miraklMarketplacePlatformOperatorApiClient);
    }

    @When("^the IBAN has been modified in Mirakl$")
    public void theIBANHasBeenModifiedInMirakl(DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        cucumberMap.put("iban", cucumberTable.get(0).get("iban"));

        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        MiraklShop updatedMiraklShop = miraklUpdateShopApi
            .updateShopsIbanNumberOnly(createdShop, createdShop.getId(), miraklMarketplacePlatformOperatorApiClient, cucumberTable);

        // map needs to be cleared as we've updated the shop
        cucumberMap.remove("createdShop");
        cucumberMap.put("createdShop", updatedMiraklShop);
    }

    @And("^a new IBAN has been provided by the seller in Mirakl and the mandatory IBAN fields have been provided$")
    public void aNewIBANHasBeenProvidedByTheSellerInMiraklAndTheMandatoryIBANFieldsHaveBeenProvided() {
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        MiraklShop updatedMiraklShop = miraklUpdateShopApi
            .updateShopToAddBankDetails(createdShop, createdShop.getId(), miraklMarketplacePlatformOperatorApiClient);

        // map needs to be cleared as we've updated the shop
        cucumberMap.remove("createdShop");
        cucumberMap.put("createdShop", updatedMiraklShop);
    }

    @And("^Mirakl has been updated with a taxId$")
    public void miraklHasBeenUpdatedWithATaxId() {
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        MiraklShop updatedMiraklShop = miraklUpdateShopApi
            .updateShopToIncludeVATNumber(createdShop, createdShop.getId(), miraklMarketplacePlatformOperatorApiClient);
        // map needs to be cleared as we've updated the shop
        cucumberMap.remove("createdShop");
        cucumberMap.put("createdShop", updatedMiraklShop);
    }

    @When("^the Mirakl Shop Details have been updated as the same as before$")
    public void theMiraklShopDetailsHaveBeenUpdatedAsTheSameAsBefore(DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        cucumberTable.forEach(row ->
            miraklUpdateShopApi
                .updateExistingShopsContactInfoWithTableData(createdShop, createdShop.getId(), miraklMarketplacePlatformOperatorApiClient, row)
        );
    }

    @When("^we update the shop by adding more shareholder data$")
    public void weUpdateTheShopByAddingMoreShareholderData(DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");

        MiraklShop updatedMiraklShop = miraklUpdateShopApi
            .addMoreUbosToShop(createdShop, createdShop.getId(), miraklMarketplacePlatformOperatorApiClient, cucumberTable);

        // map needs to be cleared as we've updated the shop
        cucumberMap.remove("createdShop");
        cucumberMap.put("createdShop", updatedMiraklShop);
    }

    @When("^the shareholder data has been updated in Mirakl$")
    public void theShareholderDataHasBeenUpdatedInMirakl(DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        MiraklShop updatedMiraklShop = miraklUpdateShopApi
            .updateUboData(createdShop, createdShop.getId(), miraklMarketplacePlatformOperatorApiClient, cucumberTable);

        // map needs to be cleared as we've updated the shop
        cucumberMap.remove("createdShop");
        cucumberMap.put("createdShop", updatedMiraklShop);
    }
}
