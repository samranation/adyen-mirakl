package com.adyen.mirakl.listeners;

import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.events.AdyenNotifcationEvent;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.mirakl.service.MailService;
import com.adyen.notification.NotificationHandler;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdyenNotificationListenerTest {

    private AdyenNotificationListener adyenNotificationListener;

    @Mock
    private AdyenNotificationRepository adyenNotificationRepositoryMock;
    @Mock
    private AdyenNotifcationEvent eventMock;
    @Mock
    private AdyenNotification adyenNotificationMock;
    @Mock
    private MailService mailServiceMock;

    @Before
    public void setup(){
        adyenNotificationListener = new AdyenNotificationListener(new NotificationHandler(), adyenNotificationRepositoryMock, mailServiceMock);
    }

    @Test
    public void doThing() throws IOException {
        URL url = Resources.getResource("adyenRequests/adyenRequestExample.json");
        final String adyenRequestJson = Resources.toString(url, Charsets.UTF_8);

        when(eventMock.getDbId()).thenReturn(1L);
        when(adyenNotificationRepositoryMock.findOneById(1L)).thenReturn(adyenNotificationMock);
        when(adyenNotificationMock.getRawAdyenNotification()).thenReturn(adyenRequestJson);

        adyenNotificationListener.handleContextRefresh(eventMock);

        verify(mailServiceMock).sendEmailFromTemplateNoUser(Locale.ENGLISH, "todoFindOutEmail", "bankAccountVerificationEmail", "email.bank.verification.title");
    }





}
