package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.StartUpTestingHook;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklShopApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklUpdateShopApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.restassured.RestAssuredAdyenApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.AssertionHelper;
import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.adyen.mirakl.service.ShopService;
import com.adyen.model.marketpay.GetAccountHolderRequest;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.service.Account;
import com.adyen.service.exception.ApiException;
import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.MiraklApiSteps.foundShop;
import static junit.framework.TestCase.fail;
import static org.awaitility.Awaitility.await;

public class AdyenAccountManagementSteps extends StepDefsHelper {

    private static final Logger log = LoggerFactory.getLogger(AdyenAccountManagementSteps.class);

    @Resource
    private StartUpTestingHook startUpCucumberHook;
    @Resource
    private Account adyenAccountService;
    @Resource
    private ShopService shopService;
    @Resource
    private MiraklShopApi miraklShopApi;
    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Resource
    private RestAssuredAdyenApi restAssuredAdyenApi;
    @Resource
    private MiraklUpdateShopApi miraklUpdateShopsApi;
    @Resource
    private AssertionHelper assertionHelper;

    private String notification;
    private String shopId;
    private Map<String, Object> mappedAdyenNotificationResponse;
    private String email;
    private MiraklShop miraklShop;


    @Given("^a new shop has been created in Mirakl for an (.*)$")
    public void aNewShopHasBeenCreatedInMiraklForAnIndividual(String legalEntity, DataTable table) throws Throwable {
        final List<Map<Object, Object>> rows = table.getTableConverter().toMaps(table, String.class, String.class);
        createdShops = miraklShopApi.createShopForIndividual(miraklMarketplacePlatformOperatorApiClient, rows, legalEntity);
    }

    @When("^a new shop has been created in Mirakl for a (.*)")
    public void aNewShopHasBeenCreatedInMiraklForABusiness(String legalEntity, DataTable table) throws Throwable {
        final List<Map<Object, Object>> rows = table.getTableConverter().toMaps(table, String.class, String.class);
        createdShops = miraklShopApi.createBusinessShopWithUbos(miraklMarketplacePlatformOperatorApiClient, rows, legalEntity);
    }

    @Then("^we process the data and push to Adyen$")
    public void adyenWillProcessTheData() {
        shopService.retrieveUpdatedShops();
    }

    @And("^an AccountHolder will be created in Adyen with status Active$")
    public void anAccountHolderWillBeCreatedInAdyenWithStatusActive() throws Throwable {
        GetAccountHolderRequest accountHolderRequest = new GetAccountHolderRequest();
        email = createdShops.getShopReturns().iterator().next().getShopCreated().getContactInformation().getEmail();
        MiraklShop miraklShop = miraklShopApi.filterMiraklShopsByEmailAndReturnShop(miraklMarketplacePlatformOperatorApiClient, email);
        accountHolderRequest.setAccountHolderCode(miraklShop.getId());

        try{
            GetAccountHolderResponse accountHolderResponse = adyenAccountService.getAccountHolder(accountHolderRequest);
            Assertions.assertThat(accountHolderResponse.getAccountHolderStatus().getStatus().toString()).isEqualTo("Active");
        }catch (ApiException e){
            log.error("Failing test due to exception", e);
            fail(e.getError().toString());
        }
    }

    @And("^a notification will be sent pertaining to (.*)$")
    public void aNotificationWillBeSentPertainingToACCOUNT_HOLDER_CREATED(String notification) {
        this.notification = notification;
        waitForNotification();
        await().atMost(Duration.TWO_MINUTES).untilAsserted(() -> {
            shopId = createdShops.getShopReturns()
                .iterator()
                .next().getShopCreated().getId();
            mappedAdyenNotificationResponse = restAssuredAdyenApi.getAdyenNotificationBody(startUpCucumberHook.getBaseRequestBinUrlPath(), shopId, notification, null);
            Assertions.assertThat(getMappedAdyenNotificationResponse()).isNotNull();
            Assertions.assertThat(((Map)getMappedAdyenNotificationResponse().get("content")).get("accountHolderCode")).isEqualTo(shopId);
            Assertions.assertThat(getMappedAdyenNotificationResponse().get("eventType")).isEqualTo(this.notification);
        });
    }

    @When("^a complete shareholder is not provided$")
    public void aCompleteShareholderIsNotProvided() {
        // UBOs were not provided in this test.
    }

    @Then("^no account holder is created in Adyen$")
    public void noAccountHolderIsCreatedInAdyen() {
        waitForNotification();
        await().atMost(Duration.ONE_MINUTE).untilAsserted(() -> {
            shopId = createdShops.getShopReturns()
                .iterator()
                .next().getShopCreated().getId();
            Map mapResult = restAssuredAdyenApi.getAdyenNotificationBody(startUpCucumberHook.getBaseRequestBinUrlPath(), shopId, "ACCOUNT_HOLDER_CREATED", null);
            Assertions.assertThat(mapResult == null);
        });
    }

    @And("^the shop data is correctly mapped to the Adyen Account$")
    public void theShopDataIsCorrectlyMappedToTheAdyenAccount() {
        email = createdShops.getShopReturns().iterator().next().getShopCreated().getContactInformation().getEmail();
        miraklShop = miraklShopApi.filterMiraklShopsByEmailAndReturnShop(miraklMarketplacePlatformOperatorApiClient, email);

        ImmutableList<String> adyen = assertionHelper.adyenAccountDataBuilder(getMappedAdyenNotificationResponse()).build();
        ImmutableList<String> mirakl = assertionHelper.miraklShopDataBuilder(email, miraklShop).build();
        Assertions.assertThat(adyen).containsAll(mirakl);
    }

    @And("^the account holder is created in Adyen with status Active$")
    public void theAccountHolderIsCreatedInAdyenWithStatusActive()  {
        Assertions.assertThat(JsonPath.parse(getMappedAdyenNotificationResponse().get("content"))
            .read("['accountHolderStatus']['status']").toString()).isEqualTo("Active");
    }

    @And("^the shop data is correctly mapped to the Adyen Business Account$")
    public void theShopDataIsCorrectlyMappedToTheAdyenBusinessAccount()  {
        email = createdShops.getShopReturns().iterator().next().getShopCreated().getContactInformation().getEmail();
        miraklShop = miraklShopApi.filterMiraklShopsByEmailAndReturnShop(miraklMarketplacePlatformOperatorApiClient, email);

        ImmutableList<String> adyen = assertionHelper.adyenShareHolderAccountDataBuilder(getMappedAdyenNotificationResponse()).build();

        ImmutableList<String> mirakl = assertionHelper.miraklShopShareHolderDataBuilder(miraklShop).build();
        Assertions.assertThat(adyen).containsAll(mirakl);
    }

    public Map<String, Object> getMappedAdyenNotificationResponse() {
        return this.mappedAdyenNotificationResponse;
    }

    @When("^the Mirakl Shop Details have been updated$")
    public void theMiraklShopDetailsHaveBeenUpdated(DataTable table) throws Throwable {
        final List<Map<Object, Object>> rows = table.getTableConverter().toMaps(table, String.class, String.class);

        MiraklShop miraklShop = retrieveMiraklShopByFiltersOnShopEmail(createdShops, miraklMarketplacePlatformOperatorApiClient, miraklShopApi);
        miraklUpdateShopsApi.updateExistingShopsContactInfoWithTableData(miraklShop, miraklShop.getId(), miraklMarketplacePlatformOperatorApiClient, rows);
    }

    @And("^a notification of (.*) will not be sent$")
    public void aNotificationOfACCOUNT_HOLDER_UPDATEDWillNotBeSent(String notification) throws Throwable {
        waitForNotification();
        await().atMost(Duration.TWO_MINUTES).untilAsserted(() -> {
            shopId = foundShop.getId();
            mappedAdyenNotificationResponse = restAssuredAdyenApi.getAdyenNotificationBody(startUpCucumberHook.getBaseRequestBinUrlPath(), shopId, notification, null);
            Assertions.assertThat(getMappedAdyenNotificationResponse()).isNull();
        });
    }
}
