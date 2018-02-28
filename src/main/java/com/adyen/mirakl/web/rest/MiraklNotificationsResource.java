package com.adyen.mirakl.web.rest;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
     * POST payout
     */
    @PostMapping("/payout")
    public String receiveNotifications(@RequestPart("file") MultipartFile csvdata) throws IOException {
        String content = new String(csvdata.getBytes());
        if (! content.isEmpty()) {
            payoutService.parseMiraklCsv(content);
        }
        return content;
    }

}
