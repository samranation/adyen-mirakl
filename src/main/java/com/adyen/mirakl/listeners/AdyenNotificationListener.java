package com.adyen.mirakl.listeners;

import com.adyen.mirakl.events.AdyenNotifcationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AdyenNotificationListener {

    @Async
    @EventListener
    public void handleContextRefresh(AdyenNotifcationEvent event) {
        System.out.println(event.getDbId());
    }

}
