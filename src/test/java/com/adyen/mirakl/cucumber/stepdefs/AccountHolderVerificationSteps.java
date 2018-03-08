package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.StartUpTestingHook;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklShopApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi.MiraklUpdateShopApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.restassured.RestAssuredAdyenApi;
import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.AssertionHelper;
import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.*;
import static org.awaitility.Awaitility.await;


public class AccountHolderVerificationSteps extends StepDefsHelper {

    @Resource
    private MiraklShopApi miraklShopApi;
    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Resource
    private StartUpTestingHook startUpTestingHook;
    @Resource
    private RestAssuredAdyenApi restAssuredAdyenApi;
    @Resource
    private AssertionHelper assertionHelper;
    @Resource
    private MiraklUpdateShopApi miraklUpdateShopsApi;

    private MiraklShop miraklShop;
    private Map<String, Object> adyenNotificationBody;
    private MiraklCreatedShops createdShops;

    @Given("^a shop has been created in Mirakl for an (.*) with Bank Information$")
    public void aShopHasBeenCreatedInMiraklForAnIndividualWithBankInformation(String legalEntity, DataTable table) {
        cucumberTable.put("table", table);
        MiraklCreatedShops shopForIndividualWithBankDetails = miraklShopApi.createShopForIndividualWithBankDetails(miraklMarketplacePlatformOperatorApiClient, rows(), legalEntity);
        cucumberMap.put("createdShops", shopForIndividualWithBankDetails );
    }

    @Then("^the (.*) notification is sent by Adyen comprising of (.*) and (.*)")
    public void theACCOUNT_HOLDER_VERIFICATIONNotificationIsSentByAdyenComprisingOfBANK_ACCOUNT_VERIFICATIONAndPASSED(String notification,
                                                                                                                      String verificationType,
                                                                                                                      String verificationStatus) {
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        String shopId = createdShops.getShopReturns().iterator().next().getShopCreated().getId();
        waitForNotification();
        await().untilAsserted(() -> {

            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), shopId, notification, verificationType);

            Assertions.assertThat(adyenNotificationBody).withFailMessage("No data received from notification endpoint").isNotNull();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationStatus").toString()).isEqualTo(verificationStatus);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationType").toString()).isEqualTo(verificationType);
        });
    }

    @Then("^a new bankAccountDetail will be created for the existing Account Holder$")
    public void aNewBankAccountDetailWillBeCreatedForTheExistingAccountHolder(DataTable table) throws Throwable {
        cucumberTable.put("table", table);
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        String shopId = createdShops.getShopReturns().iterator().next().getShopCreated().getId();
        String email = createdShops.getShopReturns().iterator().next().getShopCreated().getContactInformation().getEmail();
        miraklShop = miraklShopApi.filterMiraklShopsByEmailAndReturnShop(miraklMarketplacePlatformOperatorApiClient, email);
        waitForNotification();
        await().untilAsserted(() -> {
            String eventType = rows().get(0).get("eventType").toString();
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), shopId, eventType, null);

            Assertions.assertThat(adyenNotificationBody).withFailMessage("Notification has not been sent yet.").isNotNull();

            List<Map<Object, Object>> bankAccountDetails = JsonPath.parse(adyenNotificationBody.get("content")).read("accountHolderDetails.bankAccountDetails");

            ImmutableList<String> miraklBankAccountDetail = assertionHelper.miraklBankAccountInformation(miraklShop).build();
            ImmutableList<String> adyenBankAccountDetail = assertionHelper.adyenBankAccountDetail(bankAccountDetails).build();
            Assertions.assertThat(miraklBankAccountDetail).containsAll(adyenBankAccountDetail);

            DocumentContext bankAccountDetail = (DocumentContext) cucumberMap.get("bankAccountDetail");
            Assertions.assertThat(bankAccountDetail.read("primaryAccount").toString()).isEqualTo("true");
            Assertions.assertThat(bankAccountDetail.read("bankAccountUUID").toString()).isNotEmpty();
        });
    }

    @And("^a new IBAN has been provided by the seller in Mirakl and the mandatory IBAN fields have been provided$")
    public void aNewIBANHasBeenProvidedByTheSellerInMiraklAndTheMandatoryIBANFieldsHaveBeenProvided() throws Throwable {
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        MiraklShop miraklShop = retrieveMiraklShopByFiltersOnShopEmail(createdShops, miraklMarketplacePlatformOperatorApiClient, miraklShopApi);
        miraklUpdateShopsApi.updateShopToAddBankDetails(miraklShop, miraklShop.getId(), miraklMarketplacePlatformOperatorApiClient);
    }

    @When("^the IBAN has been modified in Mirakl$")
    public void theIBANHasBeenModifiedInMirakl(DataTable table) throws Throwable {
        cucumberTable.put("table", table);
        cucumberMap.put("iban", rows().get(0).get("iban"));
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        MiraklShop miraklShop = retrieveMiraklShopByFiltersOnShopEmail(createdShops, miraklMarketplacePlatformOperatorApiClient, miraklShopApi);
        miraklUpdateShopsApi.updateShopsIbanNumberOnly(miraklShop, miraklShop.getId(), miraklMarketplacePlatformOperatorApiClient, rows());
    }

    @And("^the previous BankAccountDetail will be removed$")
    public void thePreviousBankAccountDetailWillBeRemoved() throws Throwable {
        Map<String, Object> content = JsonPath.parse(adyenNotificationBody.get("content")).read("accountHolderDetails.bankAccountDetails[0]");
        Assertions.assertThat(content).hasSize(1);
    }

    @Then("^adyen will send the (.*) comprising of (\\w*) and status of (.*)")
    public void adyenWillSendTheACCOUNT_HOLDER_VERIFICATIONComprisingOfCOMPANY_VERIFICATION(String eventType, String verificationType, String status) throws Throwable {
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        String shopId = createdShops.getShopReturns().iterator().next().getShopCreated().getId();

        waitForNotification();
        await().untilAsserted(() -> {
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), shopId, eventType, verificationType);
            Assertions.assertThat(adyenNotificationBody).isNotEmpty();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationType").toString()).isEqualTo(verificationType);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationStatus").toString()).isEqualTo(status);
        });
    }

    @Then("^adyen will send the (.*) comprising of accountHolder (.*) and status of (.*)")
    public void adyenWillSendTheACCOUNT_HOLDER_VERIFICATIONComprisingOfCOMPANY_VERIFICATIONAccountHolder(String eventType, String verificationType, String status) throws Throwable {
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        String shopId = createdShops.getShopReturns().iterator().next().getShopCreated().getId();

        waitForNotification();
        await().untilAsserted(() -> {
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), shopId, eventType, verificationType);
            Assertions.assertThat(adyenNotificationBody).isNotEmpty();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verification.accountHolder.checks[0].type").toString()).isEqualTo(verificationType);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verification.accountHolder.checks[0].status").toString()).isEqualTo(status);
        });
    }

    @And("^Mirakl has been updated with a taxId$")
    public void miraklHasBeenUpdatedWithATaxId() throws Throwable {
        createdShops = (MiraklCreatedShops) cucumberMap.get("createdShops");
        MiraklShop miraklShop = retrieveMiraklShopByFiltersOnShopEmail(createdShops, miraklMarketplacePlatformOperatorApiClient, miraklShopApi);
        miraklUpdateShopsApi.updateShopToIncludeVATNumber(miraklShop, miraklShop.getId(), miraklMarketplacePlatformOperatorApiClient);
    }
}
