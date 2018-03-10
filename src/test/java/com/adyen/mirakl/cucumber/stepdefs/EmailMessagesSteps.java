package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;

public class EmailMessagesSteps extends StepDefsHelper {
    @Then("^an email will be sent to the seller$")
    public void anEmailWillBeSentToTheSeller(String message) throws Throwable {
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        String email = createdShop.getContactInformation().getEmail();

        ResponseBody responseBody = RestAssured.get(mailTrapConfiguration.mailTrapEndPoint()).thenReturn().body();
        List<Map<String, Object>> emailLists = responseBody.jsonPath().get();

        String htmlBody = null;
        for (Map list : emailLists) {
            if (list.get("to_email").equals(email)) {
                htmlBody = list.get("html_body").toString();
                break;
            }
        }
        if (htmlBody != null) {
            Document parse = Jsoup.parse(htmlBody);
            Assertions.assertThat(parse.body().text()).contains(message);
        }
    }
}
