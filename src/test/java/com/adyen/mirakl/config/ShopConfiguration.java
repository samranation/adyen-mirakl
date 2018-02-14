package com.adyen.mirakl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "shops", ignoreUnknownFields = false)
public class ShopConfiguration {

    public Map<String, Integer> shopIds;

    public Map<String, Integer> getShopIds() {
        return shopIds;
    }

    public void setShopIds(Map<String, Integer> shopIds) {
        this.shopIds = shopIds;
    }
}
