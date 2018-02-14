package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.config.ShopConfiguration;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;

public class MiraklApiSteps extends StepDefs {

    private final Logger log = LoggerFactory.getLogger(MiraklApiSteps.class);

    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Resource
    private ShopConfiguration shopConfiguration;
    private String additionalFieldName;
    private String seller;
    private String legalEntity;

    @Given("^the operator has specified that the (.*) is an (.*)")
    public void theOperatorHasSpecifiedThatTheSellerIsAnLegalEntity(String seller, String legalEntity) throws Throwable {
        this.seller = shopConfiguration.shopIds.get(seller).toString();
        this.legalEntity = legalEntity;
    }

    @When("^the operator views the shop information using S20 Mirakl API call$")
    public void theOperatorViewsTheShopInformationUsingSMiraklAPICall() throws Throwable {
        MiraklGetShopsRequest shopsRequest = new MiraklGetShopsRequest();

        shopsRequest.setPaginate(false);

        MiraklShops shops = miraklMarketplacePlatformOperatorApiClient.getShops(shopsRequest);

        MiraklShop miraklShop = shops.getShops().stream()
            .filter(shop -> seller.equals(shop.getId())).findAny()
            .orElseThrow(() -> new IllegalStateException("Cannot find shop"));

        additionalFieldName = miraklShop.getAdditionalFieldValues().stream()
            .filter(addFields -> addFields instanceof MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::cast)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue)
            .filter(legalEntity::equalsIgnoreCase)
            .findAny().orElse(null);
    }

    @Then("^the sellers legal entity will be displayed as (.*)$")
    public void theSellersLegalEntityWillBeDisplayedAsLegalEntity(String legalEntity) throws Throwable {
        Assertions.assertThat(additionalFieldName).isEqualToIgnoringCase(legalEntity);
    }
}
