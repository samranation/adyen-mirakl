package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.google.common.io.Resources;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;

import java.io.File;
import java.net.URL;

public class MiraklPaymentVoucherSteps extends StepDefsHelper {

    @When("^a payment voucher is sent to the App from Mirakl$")
    public void aPaymentVoucherIsSentToAdyen() throws Throwable {
        URL url = Resources.getResource("paymentvouchers/PaymentVoucher_PayoutShop01.csv");
        RestAssured
            .given()
                .multiPart(new File(url.getPath()))
            .when()
                .post("/api/mirakl-notifications/payout")
            .then()
                .statusCode(200);
    }
}
