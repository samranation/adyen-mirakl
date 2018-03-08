package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.*;
import static org.awaitility.Awaitility.await;

public class AccountHolderVerificationSteps extends StepDefsHelper {

    private Map<String, Object> adyenNotificationBody;

    @Then("^the (.*) notification is sent by Adyen comprising of (.*) and (.*)")
    public void theACCOUNT_HOLDER_VERIFICATIONNotificationIsSentByAdyenComprisingOfBANK_ACCOUNT_VERIFICATIONAndPASSED(String notification,
                                                                                                                      String verificationType,
                                                                                                                      String verificationStatus) {
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");
        waitForNotification();
        await().untilAsserted(() -> {
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), createdShop.getId(), notification, verificationType);

            Assertions.assertThat(adyenNotificationBody).withFailMessage("No data received from notification endpoint").isNotNull();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationStatus").toString()).isEqualTo(verificationStatus);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationType").toString()).isEqualTo(verificationType);
        });
    }

    @Then("^a new bankAccountDetail will be created for the existing Account Holder$")
    public void aNewBankAccountDetailWillBeCreatedForTheExistingAccountHolder(DataTable table) {
        cucumberTable.put("table", table);
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        waitForNotification();
        await().untilAsserted(() -> {
            String eventType = rows().get(0).get("eventType").toString();
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), createdShop.getId(), eventType, null);

            Assertions.assertThat(adyenNotificationBody).withFailMessage("Notification has not been sent yet.").isNotNull();

            List<Map<Object, Object>> bankAccountDetails = JsonPath.parse(adyenNotificationBody
                .get("content")).read("accountHolderDetails.bankAccountDetails");

            ImmutableList<String> miraklBankAccountDetail = assertionHelper.miraklBankAccountInformation(createdShop).build();
            ImmutableList<String> adyenBankAccountDetail = assertionHelper.adyenBankAccountDetail(bankAccountDetails).build();
            Assertions.assertThat(miraklBankAccountDetail).containsAll(adyenBankAccountDetail);

            DocumentContext bankAccountDetail = (DocumentContext) cucumberMap.get("bankAccountDetail");
            Assertions.assertThat(bankAccountDetail.read("primaryAccount").toString()).isEqualTo("true");
            Assertions.assertThat(bankAccountDetail.read("bankAccountUUID").toString()).isNotEmpty();
        });
    }

    @And("^the previous BankAccountDetail will be removed$")
    public void thePreviousBankAccountDetailWillBeRemoved() {
        Map<String, Object> content = JsonPath.parse(adyenNotificationBody.get("content")).read("accountHolderDetails.bankAccountDetails[0]");
        Assertions.assertThat(content).hasSize(1);
    }

    @Then("^adyen will send the (.*) comprising of (\\w*) and status of (.*)")
    public void adyenWillSendTheACCOUNT_HOLDER_VERIFICATIONComprisingOfCOMPANY_VERIFICATION(String eventType, String verificationType, String status) throws Throwable {
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");

        waitForNotification();
        await().untilAsserted(() -> {
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), createdShop.getId(), eventType, verificationType);
            Assertions.assertThat(adyenNotificationBody).isNotEmpty();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationType").toString()).isEqualTo(verificationType);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verificationStatus").toString()).isEqualTo(status);
        });
    }

    @Then("^adyen will send the (.*) comprising of accountHolder (.*) and status of (.*)")
    public void adyenWillSendTheACCOUNT_HOLDER_VERIFICATIONComprisingOfCOMPANY_VERIFICATIONAccountHolder(String eventType, String verificationType, String status) throws Throwable {
        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");

        waitForNotification();
        await().untilAsserted(() -> {
            adyenNotificationBody = restAssuredAdyenApi
                .getAdyenNotificationBody(startUpTestingHook.getBaseRequestBinUrlPath(), createdShop.getId(), eventType, verificationType);
            Assertions.assertThat(adyenNotificationBody).isNotEmpty();
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verification.accountHolder.checks[0].type").toString()).isEqualTo(verificationType);
            Assertions.assertThat(JsonPath.parse(adyenNotificationBody.get("content"))
                .read("verification.accountHolder.checks[0].status").toString()).isEqualTo(status);
        });
    }
}
