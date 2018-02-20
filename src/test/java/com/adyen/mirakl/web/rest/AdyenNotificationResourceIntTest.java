package com.adyen.mirakl.web.rest;

import com.adyen.mirakl.AdyenMiraklConnectorApp;

import com.adyen.mirakl.domain.AdyenNotification;
import com.adyen.mirakl.repository.AdyenNotificationRepository;
import com.adyen.mirakl.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.adyen.mirakl.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AdyenNotificationResource REST controller.
 *
 * @see AdyenNotificationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class AdyenNotificationResourceIntTest {

    private static final String DEFAULT_RAW_ADYEN_NOTIFICATION = "AAAAAAAAAA";

    @Autowired
    private AdyenNotificationRepository adyenNotificationRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restAdyenNotificationMockMvc;

    private AdyenNotification adyenNotification;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AdyenNotificationResource adyenNotificationResource = new AdyenNotificationResource(adyenNotificationRepository);
        this.restAdyenNotificationMockMvc = MockMvcBuilders.standaloneSetup(adyenNotificationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AdyenNotification createEntity(EntityManager em) {
        AdyenNotification adyenNotification = new AdyenNotification()
            .rawAdyenNotification(DEFAULT_RAW_ADYEN_NOTIFICATION);
        return adyenNotification;
    }

    @Before
    public void initTest() {
        adyenNotification = createEntity(em);
    }

    @Test
    @Transactional
    public void createAdyenNotification() throws Exception {
        int databaseSizeBeforeCreate = adyenNotificationRepository.findAll().size();

        // Create the AdyenNotification
        restAdyenNotificationMockMvc.perform(post("/api/adyen-notifications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(adyenNotification)))
            .andExpect(status().isCreated());

        // Validate the AdyenNotification in the database
        List<AdyenNotification> adyenNotificationList = adyenNotificationRepository.findAll();
        assertThat(adyenNotificationList).hasSize(databaseSizeBeforeCreate + 1);
        AdyenNotification testAdyenNotification = adyenNotificationList.get(adyenNotificationList.size() - 1);
        assertThat(testAdyenNotification.getRawAdyenNotification()).isEqualTo(DEFAULT_RAW_ADYEN_NOTIFICATION);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdyenNotification.class);
        AdyenNotification adyenNotification1 = new AdyenNotification();
        adyenNotification1.setId(1L);
        AdyenNotification adyenNotification2 = new AdyenNotification();
        adyenNotification2.setId(adyenNotification1.getId());
        assertThat(adyenNotification1).isEqualTo(adyenNotification2);
        adyenNotification2.setId(2L);
        assertThat(adyenNotification1).isNotEqualTo(adyenNotification2);
        adyenNotification1.setId(null);
        assertThat(adyenNotification1).isNotEqualTo(adyenNotification2);
    }
}
