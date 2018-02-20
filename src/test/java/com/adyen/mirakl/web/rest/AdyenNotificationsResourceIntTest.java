package com.adyen.mirakl.web.rest;

import com.adyen.mirakl.AdyenMiraklConnectorApp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Test class for the AdyenNotifications REST controller.
 *
 * @see AdyenNotificationsResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class AdyenNotificationsResourceIntTest {

    private MockMvc restMockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        AdyenNotificationsResource adyenNotificationsResource = new AdyenNotificationsResource();
        restMockMvc = MockMvcBuilders
            .standaloneSetup(adyenNotificationsResource)
            .build();
    }

    /**
    * Test receiveNotifications
    */
    @Test
    public void testReceiveNotifications() throws Exception {
        restMockMvc.perform(post("/api/adyen-notifications/receive-notifications"))
            .andExpect(status().isOk());
    }

}
