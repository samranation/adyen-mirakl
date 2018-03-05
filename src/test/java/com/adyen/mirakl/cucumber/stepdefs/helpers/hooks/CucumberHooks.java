package com.adyen.mirakl.cucumber.stepdefs.helpers.hooks;

import cucumber.api.java.Before;

import java.util.HashMap;
import java.util.Map;

public class CucumberHooks {

    public static Map<String, Object> worldMap;

    @Before
    public void beforeScenario() {
        worldMap = new HashMap<>();
    }
}
