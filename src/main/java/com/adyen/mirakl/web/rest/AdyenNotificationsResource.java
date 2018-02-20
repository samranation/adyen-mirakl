package com.adyen.mirakl.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdyenNotifications controller
 */
@RestController
@RequestMapping("/api/adyen-notifications")
public class AdyenNotificationsResource {

    private final Logger log = LoggerFactory.getLogger(AdyenNotificationsResource.class);

    /**
    * POST receiveNotifications
    */
    @PostMapping("/receive-notifications")
    public String receiveNotifications() {
        return "receiveNotifications";
    }

}
