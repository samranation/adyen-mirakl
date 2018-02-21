package com.adyen.mirakl.events;

import org.springframework.context.ApplicationEvent;

public class AdyenNotifcationEvent extends ApplicationEvent {

    private Long dbId;

    /**
     * Create a new ApplicationEvent.
     *
     * @param dbId the object on which the event initially occurred (never {@code null})
     */
    public AdyenNotifcationEvent(final Long dbId) {
        super(dbId);
        this.dbId = dbId;
    }

    public Long getDbId() {
        return dbId;
    }
}
