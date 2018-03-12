package com.adyen.mirakl.listeners;

import com.adyen.mirakl.config.MailTemplateService;
import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.events.AdyenNotifcationEvent;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.notification.NotificationHandler;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    private MailTemplateService mailTemplateServiceMock;
    @Mock
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;
    @Mock
    private MiraklShop miraklShopMock;
    @Mock
    private MiraklShops miraklShopsMock;
    @Captor
    private ArgumentCaptor<MiraklGetShopsRequest> miraklShopsRequestCaptor;

    @Before
    public void setup(){
        adyenNotificationListener = new AdyenNotificationListener(new NotificationHandler(), adyenNotificationRepositoryMock, mailTemplateServiceMock, miraklMarketplacePlatformOperatorApiClient);
    }

    @Test
    public void sendEmail() throws IOException {
        URL url = Resources.getResource("adyenRequests/adyenRequestExample.json");
        final String adyenRequestJson = Resources.toString(url, Charsets.UTF_8);

        when(eventMock.getDbId()).thenReturn(1L);
        when(adyenNotificationRepositoryMock.findOneById(1L)).thenReturn(adyenNotificationMock);
        when(adyenNotificationMock.getRawAdyenNotification()).thenReturn(adyenRequestJson);
        when(miraklMarketplacePlatformOperatorApiClient.getShops(miraklShopsRequestCaptor.capture())).thenReturn(miraklShopsMock);
        when(miraklShopsMock.getShops()).thenReturn(ImmutableList.of(miraklShopMock));

        adyenNotificationListener.handleContextRefresh(eventMock);

        final MiraklGetShopsRequest miraklGetShopRequest = miraklShopsRequestCaptor.getValue();
        Assertions.assertThat(miraklGetShopRequest.getShopIds()).containsOnly("2146");
        verify(mailTemplateServiceMock).sendMiraklShopEmailFromTemplate(miraklShopMock, Locale.ENGLISH, "bankAccountVerificationEmail", "email.bank.verification.title");
        verify(adyenNotificationRepositoryMock).delete(1L);
    }





}
