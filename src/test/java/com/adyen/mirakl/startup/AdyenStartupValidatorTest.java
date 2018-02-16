package com.adyen.mirakl.startup;

import com.adyen.model.marketpay.notification.GetNotificationConfigurationListResponse;
import com.adyen.model.marketpay.notification.NotificationConfigurationDetails;
import com.adyen.model.marketpay.notification.UpdateNotificationConfigurationRequest;
import com.adyen.model.marketpay.notification.UpdateNotificationConfigurationResponse;
import com.adyen.service.Notification;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdyenStartupValidatorTest {

    @InjectMocks
    private AdyenStartupValidator testObj;
    @Mock
    private ContextRefreshedEvent eventMock;
    @Mock
    private NotificationConfigurationDetails notificationFromAdyenMock1, notificationFromAdyenMock2, notificationFromAdyenMock3;
    @Mock
    private Notification adyenNotificationMock;
    @Mock
    private GetNotificationConfigurationListResponse getNotificationConfigurationListResponseMock;
    @Mock
    private UpdateNotificationConfigurationResponse updateNotificationConfigurationResponse1, updateNotificationConfigurationResponse2, updateNotificationConfigurationResponse3;
    @Captor
    private ArgumentCaptor<UpdateNotificationConfigurationRequest> updateNotificationConfigurationRequestCaptor;

    @Before
    public void setUp(){
        final NotificationConfigurationDetails notificationConfigurationDetail1 = new NotificationConfigurationDetails();
        notificationConfigurationDetail1.setDescription("description1");
        final NotificationConfigurationDetails notificationConfigurationDetail2 = new NotificationConfigurationDetails();
        notificationConfigurationDetail2.setDescription("description2");
        final NotificationConfigurationDetails notificationConfigurationDetail3 = new NotificationConfigurationDetails();
        notificationConfigurationDetail3.setDescription("description4");
        testObj.setNotificationConfigurationDetails(ImmutableList.of(notificationConfigurationDetail1, notificationConfigurationDetail2, notificationConfigurationDetail3));
    }


    @Test
    public void shouldSendUpdateWithCurrentConfigInApplicationYaml() throws Exception {
        when(notificationFromAdyenMock1.getDescription()).thenReturn("description1");
        when(notificationFromAdyenMock1.getNotificationId()).thenReturn(1L);
        when(notificationFromAdyenMock2.getDescription()).thenReturn("description2");
        when(notificationFromAdyenMock2.getNotificationId()).thenReturn(2L);
        when(notificationFromAdyenMock3.getDescription()).thenReturn("description3");
        when(notificationFromAdyenMock3.getNotificationId()).thenReturn(3L);

        when(adyenNotificationMock.getNotificationConfigurationList()).thenReturn(getNotificationConfigurationListResponseMock);
        when(getNotificationConfigurationListResponseMock.getConfigurations()).thenReturn(ImmutableList.of(notificationFromAdyenMock1, notificationFromAdyenMock2));

        when(adyenNotificationMock.updateNotificationConfiguration(updateNotificationConfigurationRequestCaptor.capture()))
            .thenReturn(updateNotificationConfigurationResponse1)
            .thenReturn(updateNotificationConfigurationResponse2)
            .thenReturn(updateNotificationConfigurationResponse3);
        when(updateNotificationConfigurationResponse1.getPspReference()).thenReturn("pspReference1");
        when(updateNotificationConfigurationResponse2.getPspReference()).thenReturn("pspReference2");
        when(updateNotificationConfigurationResponse3.getPspReference()).thenReturn("pspReference3");

        testObj.onApplicationEvent(eventMock);

        final List<UpdateNotificationConfigurationRequest> allValues = updateNotificationConfigurationRequestCaptor.getAllValues();
        final UpdateNotificationConfigurationRequest updateNotificationConfigurationRequest1 = allValues.get(0);
        final UpdateNotificationConfigurationRequest updateNotificationConfigurationRequest2 = allValues.get(1);
        final UpdateNotificationConfigurationRequest updateNotificationConfigurationRequest3 = allValues.get(2);
        verify(adyenNotificationMock).updateNotificationConfiguration(updateNotificationConfigurationRequest1);
        verify(adyenNotificationMock).updateNotificationConfiguration(updateNotificationConfigurationRequest2);
        verify(adyenNotificationMock).updateNotificationConfiguration(updateNotificationConfigurationRequest3);

        Assertions.assertThat(updateNotificationConfigurationRequest1.getConfigurationDetails().getNotificationId()).isEqualTo(null);
        Assertions.assertThat(updateNotificationConfigurationRequest2.getConfigurationDetails().getNotificationId()).isEqualTo(2L);
        Assertions.assertThat(updateNotificationConfigurationRequest3.getConfigurationDetails().getNotificationId()).isEqualTo(1L);
    }

}
