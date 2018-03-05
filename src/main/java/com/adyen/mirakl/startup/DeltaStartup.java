package com.adyen.mirakl.startup;


import com.adyen.mirakl.service.DeltaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.ZonedDateTime;

@Component
@ConfigurationProperties(prefix = "miraklConfig", ignoreUnknownFields = false)
public class DeltaStartup implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(DeltaStartup.class);

    @Resource
    private DeltaService deltaService;

    private Boolean createShopDeltaAtStartup;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (createShopDeltaAtStartup){
            final ZonedDateTime now = ZonedDateTime.now();
            log.info("Creating mirakl shop delta for: {}", now);
            deltaService.createNewShopDelta(now);
        }
    }

    public void setCreateShopDeltaAtStartup(final Boolean createShopDeltaAtStartup) {
        this.createShopDeltaAtStartup = createShopDeltaAtStartup;
    }

    public void setDeltaService(final DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
