package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.*;
import static org.awaitility.Awaitility.await;

public class PayoutVerificationSteps extends StepDefsHelper {

    @Then("^adyen will send the (.*) notification$")
    public void adyenWillSendTheACCOUNT_HOLDER_PAYOUTNotification(String notification, DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        String accountHolderCode = cucumberMap.get("accountHolderCode").toString();

        waitForNotification();
        await().untilAsserted(()->{
            Map<String, Object> adyenNotificationBody = retrieveAdyenNotificationBody(notification, accountHolderCode);
            DocumentContext content = JsonPath.parse(adyenNotificationBody.get("content"));
            cucumberTable.forEach(row-> {
                Assertions.assertThat(row.get("currency"))
                    .isEqualTo(content.read("amounts[0].Amount.currency"));

                Assertions.assertThat(row.get("amount"))
                    .isEqualTo(Double.toString(content.read("amounts[0].Amount.value")));

                Assertions.assertThat(row.get("iban"))
                    .isEqualTo(content.read("bankAccountDetail.iban"));

                Assertions.assertThat(row.get("statusCode"))
                    .isEqualTo(content.read("status.statusCode"));
            });
        });
    }

    @Then("^adyen will send the (.*) notification with status$")
    public void adyenWillSendTheACCOUNT_HOLDER_PAYOUTNotificationWithStatusCode(String notification, DataTable table) {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);
        waitForNotification();
        Map<String, Object> adyenNotificationBody = retrieveAdyenNotificationBody(notification, cucumberMap.get("accountHolderCode").toString());
        DocumentContext content = JsonPath.parse(adyenNotificationBody.get("content"));
        Assertions.assertThat(cucumberTable.get(0).get("statusCode"))
            .withFailMessage("Status was not correct.")
            .isEqualTo(content.read("status.statusCode"));

        String message = cucumberTable.get(0).get("message");
        String messageWithAccountCode = String.format("%s %s", message, cucumberMap.get("accountCode").toString());

        Assertions.assertThat(content.read("status.message ").toString())
            .contains(messageWithAccountCode);
    }
}
