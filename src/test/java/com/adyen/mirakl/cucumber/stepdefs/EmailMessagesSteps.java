package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;
import static org.awaitility.Awaitility.await;

public class EmailMessagesSteps extends StepDefsHelper {
    @Then("^an email will be sent to the seller$")
    public void anEmailWillBeSentToTheSeller() {
        MiraklShop createdShop = (MiraklShop) cucumberMap.get("createdShop");
        String email = createdShop.getContactInformation().getEmail();

        await().atMost(Duration.ONE_MINUTE).untilAsserted(() -> {
                ResponseBody responseBody = RestAssured.get(mailTrapConfiguration.mailTrapEndPoint()).thenReturn().body();
                List<Map<String, Object>> emailLists = responseBody.jsonPath().get();

                String htmlBody;
                Assertions.assertThat(emailLists.size()).isGreaterThan(0);
                for (Map list : emailLists) {
                    if (list.get("to_email").equals(email)) {
                        htmlBody = list.get("html_body").toString();
                        Assertions.assertThat(email).isEqualTo(list.get("to_email"));
                        cucumberMap.put("htmlBody", htmlBody);
                        break;
                    } else {
                        Assertions.fail("Email was not found in mailtrap. Email: [%s]", email);
                    }
                }
            }
        );

        String htmlBody = cucumberMap.get("htmlBody").toString();
        Document parsedBody = Jsoup.parse(htmlBody);
        Assertions.assertThat(parsedBody.body().text())
            .contains(createdShop.getId())
            .contains(createdShop.getContactInformation().getCivility())
            .contains(createdShop.getContactInformation().getFirstname())
            .contains(createdShop.getContactInformation().getLastname());
    }
}
