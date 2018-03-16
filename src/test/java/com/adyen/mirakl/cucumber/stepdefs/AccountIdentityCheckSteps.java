package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.adyen.model.marketpay.BusinessDetails;
import com.adyen.model.marketpay.GetAccountHolderRequest;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.model.marketpay.ShareholderContact;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import net.minidev.json.JSONArray;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;
import static org.awaitility.Awaitility.await;

public class AccountIdentityCheckSteps extends StepDefsHelper {
    @Then("^adyen will send the (.*) notification with multiple (.*) of status (.*)")
    public void adyenWillSendTheACCOUNT_HOLDER_UPDATEDNotificationWithMultipleIDENTITY_VERIFICATIONOfStatusDATA_PROVIDED(
        String eventType, String verificationType, String verificationStatus) {
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");
        waitForNotification();
        await().untilAsserted(() -> {
            Map<String, Object> adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpCucumberHook.getBaseRequestBinUrlPath(), createdShop.getId(), eventType, verificationType);

            Assertions.assertThat(adyenNotificationBody).isNotEmpty();
            JSONArray shareholderJsonArray = JsonPath.parse(adyenNotificationBody).read("content.verification.shareholders.*");
            for (Object shareholder : shareholderJsonArray) {
                Object checks = JsonPath.parse(shareholder).read("checks[0]");
                Assertions.assertThat(JsonPath.parse(checks).read("type").toString()).isEqualTo(verificationType);
                Assertions.assertThat(JsonPath.parse(checks).read("status").toString()).isEqualTo(verificationStatus);
            }
        });
    }

    @Then("^adyen will send multiple (.*) notification with (.*) of status (.*)$")
    public void adyenWillSendMultipleACCOUNT_HOLDER_VERIFICATIONNotificationWithIDENTITY_VERIFICATIONOfStatusDATA_PROVIDED(
        String eventType, String verificationType, String verificationStatus) {
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");
        waitForNotification();
        await().untilAsserted(()-> {

            Map<String, Object> accountHolderCreated = restAssuredAdyenApi.getAdyenNotificationBody(
                startUpCucumberHook.getBaseRequestBinUrlPath(), createdShop.getId(), eventType, verificationType);
            Assertions.assertThat(accountHolderCreated).isNotEmpty();

            ArrayList shareholdersArray = JsonPath.parse(accountHolderCreated.get("content")).read("verification.shareholders");
            for (Object shareholder : shareholdersArray) {
                ArrayList checks = JsonPath.parse(shareholder).read("checks.*");
                Assertions
                    .assertThat(JsonPath.parse(checks).read("[0]status").toString())
                    .isEqualTo(verificationStatus);
            }
        });
    }

    @And("^getAccountHolder will have the correct amount of shareholders and data in Adyen$")
    public void getaccountholderWillHaveTheCorrectAmountOfShareholdersAndDataInAdyen(DataTable table) throws Throwable {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        String maxUbos = cucumberTable.get(0).get("maxUbos");

        GetAccountHolderRequest getAccountHolderRequest = new GetAccountHolderRequest();
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");
        getAccountHolderRequest.setAccountHolderCode(createdShop.getId());
        GetAccountHolderResponse accountHolder = adyenAccountService.getAccountHolder(getAccountHolderRequest);

        BusinessDetails businessDetails = new BusinessDetails();
        List<ShareholderContact> shareholders = businessDetails.getShareholders();
        accountHolder.getAccountHolderDetails().businessDetails(businessDetails);

        for (ShareholderContact contact : shareholders){
            Assertions
                .assertThat(createdShop.getContactInformation().getFirstname())
                .isEqualTo(contact.getName().getFirstName());
            Assertions
                .assertThat(createdShop.getContactInformation().getLastname())
                .isEqualTo(contact.getName().getLastName());
            Assertions
                .assertThat(createdShop.getContactInformation().getEmail())
                .isEqualTo(contact.getEmail());
            Assertions
                .assertThat(shareholders.size())
                .isEqualTo(Integer.valueOf(maxUbos));
        }
    }
}
