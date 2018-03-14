package com.adyen.mirakl.cucumber.stepdefs.helpers.hooks;

import cucumber.api.java.Before;
import org.awaitility.Awaitility;
import org.awaitility.Duration;

import java.util.HashMap;
import java.util.Map;

public class CucumberHooks {

    public static Map<String, Object> cucumberMap;

    @Before
    public void initNewCucumberMap() {
        cucumberMap = new HashMap<>();
    }

    @Before
    public void setDefaultAwaitilityTimeOut() {
        Awaitility.setDefaultTimeout(Duration.FIVE_MINUTES);
    }
}
