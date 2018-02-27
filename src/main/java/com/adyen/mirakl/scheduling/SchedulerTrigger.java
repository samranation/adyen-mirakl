package com.adyen.mirakl.scheduling;


import com.adyen.mirakl.service.ShopService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Profile({"dev","prod"})
public class SchedulerTrigger {

    @Resource
    private ShopService shopService;

    @Scheduled(cron = "${application.shopUpdaterCron}")
    public void runShopUpdates(){
        shopService.retrieveUpdatedShops();
    }

    public void setShopService(final ShopService shopService) {
        this.shopService = shopService;
    }
}
