package com.adyen.mirakl.cucumber.stepdefs.helpers.hooks;

import cucumber.api.DataTable;
import cucumber.api.java.Before;
import org.awaitility.Awaitility;
import org.awaitility.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CucumberHooks {

    public static Map<String, Object> cucumberMap;
    public static Map<String, Object> cucumberTable;

    @Before
    public void initNewCucumberMap() {
        cucumberMap = new HashMap<>();
    }

    @Before
    public void setDefaultAwaitilityTimeOut() {
        Awaitility.setDefaultTimeout(Duration.FIVE_MINUTES);
    }

    @Before
    public static void createNewTableMap() {
        cucumberTable = new HashMap<>();
    }

    /**
     * rows() method should not be a @Before as createNewTableMap() will initialise
     * a new HashMap before each scenario.
     *
     * @return converted cucumber table map for each cucumber table row
     */
    public static List<Map<Object, Object>> rows(){
        DataTable table = (DataTable) cucumberTable.get("table");
        return table.getTableConverter().toMaps(table, String.class, String.class);
    }
}
