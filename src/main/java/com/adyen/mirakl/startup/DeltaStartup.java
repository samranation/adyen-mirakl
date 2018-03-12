package com.adyen.mirakl.startup;


import java.time.ZonedDateTime;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import com.adyen.mirakl.service.DeltaService;

@Component
@ConfigurationProperties(prefix = "miraklConfig", ignoreUnknownFields = false)
public class DeltaStartup implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger log = LoggerFactory.getLogger(DeltaStartup.class);

    @Resource
    private DeltaService deltaService;

    private Boolean createShopDeltaAtStartup;

    private Boolean createDocDeltaAtStartup;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final ZonedDateTime now = ZonedDateTime.now();
        if (createShopDeltaAtStartup) {
            log.info("Creating mirakl shop delta for: {}", now);
            deltaService.createNewShopDelta(now);
        }
        if (createDocDeltaAtStartup) {
            log.info("Creating mirakl doc delta for: {}", now);
            deltaService.createNewDocumentDelta(now);
        }
    }

    public void setCreateShopDeltaAtStartup(final Boolean createShopDeltaAtStartup) {
        this.createShopDeltaAtStartup = createShopDeltaAtStartup;
    }

    public void setCreateDocDeltaAtStartup(final Boolean createDocDeltaAtStartup) {
        this.createDocDeltaAtStartup = createDocDeltaAtStartup;
    }

    public void setDeltaService(final DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
