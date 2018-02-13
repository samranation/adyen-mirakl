package com.adyen.mirakl.cucumber.stepdefs;

import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import javax.annotation.Resource;

public class MiraklApiSteps extends StepDefs{

    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;


    @Given("^the operator has specified that the (.*) is an (.*)")
    public void theOperatorHasSpecifiedThatTheSellerIsAnLegalEntity(String seller, String legalEntity) throws Throwable {
    }

    @When("^the operator views the shop information using S20 Mirakl API call$")
    public void theOperatorViewsTheShopInformationUsingSMiraklAPICall() throws Throwable {
    }

    @Then("^the sellers legal entity will be displayed as (.*)$")
    public void theSellersLegalEntityWillBeDisplayedAsLegalEntity(String legalEntity) throws Throwable {
    }
}
