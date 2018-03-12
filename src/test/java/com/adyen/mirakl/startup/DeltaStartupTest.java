package com.adyen.mirakl.startup;

import com.adyen.mirakl.service.DeltaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.event.ContextRefreshedEvent;

import java.time.ZonedDateTime;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeltaStartupTest {


    @InjectMocks
    private DeltaStartup testObj = new DeltaStartup();

    @Mock
    private DeltaService deltaService;
    @Mock
    private ContextRefreshedEvent eventMock;


    @Test
    public void shouldCreateDeltaIfConfigSaysSo(){
        testObj.setCreateShopDeltaAtStartup(true);
        testObj.setCreateDocDeltaAtStartup(false);

        testObj.onApplicationEvent(eventMock);

        verify(deltaService).createNewShopDelta(any(ZonedDateTime.class));
    }


    @Test
    public void shouldNoCreateDeltaIfConfigSaysSo(){
        testObj.setCreateShopDeltaAtStartup(false);
        testObj.setCreateDocDeltaAtStartup(false);

        testObj.onApplicationEvent(eventMock);

        verify(deltaService, never()).createNewShopDelta(any(ZonedDateTime.class));
    }

    @Test
    public void shouldCreateDocDeltaIfConfigSaysSo(){
        testObj.setCreateShopDeltaAtStartup(false);
        testObj.setCreateDocDeltaAtStartup(true);

        testObj.onApplicationEvent(eventMock);

        verify(deltaService).createNewDocumentDelta(any(ZonedDateTime.class));
    }


    @Test
    public void shouldNoCreateDocDeltaIfConfigSaysSo(){
        testObj.setCreateShopDeltaAtStartup(false);
        testObj.setCreateDocDeltaAtStartup(false);

        testObj.onApplicationEvent(eventMock);

        verify(deltaService, never()).createNewDocumentDelta(any(ZonedDateTime.class));
    }

}
