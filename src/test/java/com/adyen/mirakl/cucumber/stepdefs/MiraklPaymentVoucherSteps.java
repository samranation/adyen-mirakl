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

//    @Autowired
//    private UserResource userResource;
//
//    private MockMvc restUserMockMvc;

    @When("^a payment voucher is sent to the Connector$")
    public void aPaymentVoucherIsSentToAdyen(DataTable table) throws Exception {
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
//        String paymentVoucher = rows().get(0).get("paymentVoucher").toString();
//        URL url = Resources.getResource("paymentvouchers/");
//        restUserMockMvc = MockMvcBuilders.standaloneSetup(
//            new FileResource(
//                new File(url.toString()),
//                new File(url.toString()+paymentVoucher))).build();
//
//        MockMultipartFile mockMultipartFile = new MockMultipartFile(paymentVoucher, paymentVoucher.getBytes());
//
//        MockHttpServletResponse response = restUserMockMvc.perform(fileUpload("https://adyen-requestbin.herokuapp.com/17en1211").file(mockMultipartFile))
//            .andReturn().getResponse();
//        int status = response.getStatus();
//        System.out.println("\n posted!"+ status);
    }
}
