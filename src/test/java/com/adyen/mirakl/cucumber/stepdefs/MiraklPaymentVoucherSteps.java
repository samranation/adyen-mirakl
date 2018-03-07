package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.google.common.io.Resources;
import cucumber.api.DataTable;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;

import java.io.File;
import java.net.URL;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberTable;
import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.rows;

public class MiraklPaymentVoucherSteps extends StepDefsHelper {

    @When("^a payment voucher is sent to the Connector$")
    public void aPaymentVoucherIsSentToAdyen(DataTable table) {
        cucumberTable.put("table", table);
        String paymentVoucher = rows().get(0).get("paymentVoucher").toString();
        URL url = Resources.getResource("paymentvouchers/"+paymentVoucher);
        RestAssured
            .given()
                .multiPart(new File(url.getPath()))
            .when()
                .post("/api/mirakl-notifications/payout")
            .then()
                .statusCode(200);
    }
}
