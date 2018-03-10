package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;

public class EmailMessagesSteps extends StepDefsHelper {
    @Then("^an email will be sent to the seller$")
    public void anEmailWillBeSentToTheSeller() throws Throwable {
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        String email = createdShop.getContactInformation().getEmail();

        ResponseBody responseBody = RestAssured.get(mailTrapConfiguration.mailTrapEndPoint()).thenReturn().body();
        List<Map<String, Object>> emailLists = responseBody.jsonPath().get();

        // TODO: find html parser which will make it easier to find the body of the html email
        String message = null;
        for (Map list : emailLists) {
            if (list.get("to_email").equals(email)) {
                message = list.get("html_body").toString();
                break;
            } else {
                message = "";
            }
        }

        //TODO: get the actual message
        Assertions.assertThat(message)
            .contains("");
    }
}
