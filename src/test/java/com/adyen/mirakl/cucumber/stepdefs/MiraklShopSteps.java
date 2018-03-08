package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.*;

public class MiraklShopSteps extends StepDefsHelper {

    private String additionalFieldName;
    private String seller;
    private String legalEntity;
    private MiraklShop miraklShop;

    @Given("^the operator has specified that the (.*) is an (.*)")
    public void theOperatorHasSpecifiedThatTheSellerIsAnLegalEntity(String seller, String legalEntity) {
        this.seller = shopConfiguration.shopIds.get(seller).toString();
        this.legalEntity = legalEntity;
    }

    @When("^the operator views the shop information using S20 Mirakl API call$")
    public void theOperatorViewsTheShopInformationUsingSMiraklAPICall() {
        MiraklShop miraklShop = getMiraklShop(miraklMarketplacePlatformOperatorApiClient, seller);

        additionalFieldName = miraklShop.getAdditionalFieldValues().stream()
            .filter(addFields -> addFields instanceof MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::cast)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue)
            .filter(legalEntity::equalsIgnoreCase)
            .findAny().orElse(null);
    }

    @Then("^the sellers legal entity will be displayed as (.*)$")
    public void theSellersLegalEntityWillBeDisplayedAsLegalEntity(String legalEntity) {
        Assertions.assertThat(additionalFieldName).isEqualToIgnoringCase(legalEntity);
    }

    @Given("^a shop exists in Mirakl with the following fields$")
    public void aShopExistsInMirakl(DataTable table) {
        List<Map<Object, Object>> rows = table.getTableConverter().toMaps(table, String.class, String.class);

        this.seller = shopConfiguration.shopIds.get(rows.get(0).get("seller").toString()).toString();
        miraklShop = getMiraklShop(miraklMarketplacePlatformOperatorApiClient, seller);
        cucumberMap.put("createdShop", miraklShop);

        Assertions.assertThat(miraklShop.getId()).isEqualTo(this.seller);
        rows.forEach(row-> {
            Assertions.assertThat(row.get("firstName")).isEqualTo(miraklShop.getContactInformation().getFirstname());
            Assertions.assertThat(row.get("lastName")).isEqualTo(miraklShop.getContactInformation().getLastname());
            Assertions.assertThat(row.get("postCode")).isEqualTo(miraklShop.getContactInformation().getZipCode());
            Assertions.assertThat(row.get("city")).isEqualTo(miraklShop.getContactInformation().getCity());
        });
    }

    @When("^the Mirakl Shop Details have been updated as the same as before$")
    public void theMiraklShopDetailsHaveBeenUpdatedAsTheSameAsBefore(DataTable table) {
        cucumberTable.put("table", table);
        miraklUpdateShopApi.updateExistingShopsContactInfoWithTableData(miraklShop, miraklShop.getId(), miraklMarketplacePlatformOperatorApiClient, rows());
    }
}
