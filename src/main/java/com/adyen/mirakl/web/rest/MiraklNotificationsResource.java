package com.adyen.mirakl.web.rest;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.adyen.mirakl.service.PayoutService;

/**
 * MiraklNotifications controller
 */
@RestController
@RequestMapping("/api/mirakl-notifications")
public class MiraklNotificationsResource {

    private final Logger log = LoggerFactory.getLogger(MiraklNotificationsResource.class);

    private final PayoutService payoutService;

    public MiraklNotificationsResource(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * POST receiveNotifications
     */
    @PostMapping("/receive-notifications")
    public String receiveNotifications(@RequestBody String csvdata) throws IOException {
        payoutService.parseMiraklCsv(csvdata);
        return csvdata;
    }

}
