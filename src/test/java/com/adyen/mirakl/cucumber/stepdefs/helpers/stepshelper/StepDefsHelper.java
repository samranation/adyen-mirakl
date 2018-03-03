package com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper;

import com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.StartUpTestingHook;
import com.adyen.mirakl.cucumber.stepdefs.helpers.restassured.RestAssuredAdyenApi;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class StepDefsHelper {

    @Resource
    private RestAssuredAdyenApi restAssuredAdyenApi;
    @Resource
    private StartUpTestingHook startUpTestingHook;

    protected void waitUntilEndpointIsNotNull() {
        await().atMost(new Duration(30, TimeUnit.MINUTES)).untilAsserted(() -> {
            boolean endpointHasReceivedANotification = restAssuredAdyenApi.endpointHasANotification(startUpTestingHook.getBaseRequestBinUrlPath());
            Assertions.assertThat(endpointHasReceivedANotification).isTrue();
        });
    }
}
