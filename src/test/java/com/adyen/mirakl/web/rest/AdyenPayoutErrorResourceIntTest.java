package com.adyen.mirakl.web.rest;

import com.adyen.mirakl.AdyenMiraklConnectorApp;

import com.adyen.mirakl.domain.AdyenPayoutError;
import com.adyen.mirakl.repository.AdyenPayoutErrorRepository;
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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.adyen.mirakl.web.rest.TestUtil.sameInstant;
import static com.adyen.mirakl.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AdyenPayoutErrorResource REST controller.
 *
 * @see AdyenPayoutErrorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class AdyenPayoutErrorResourceIntTest {

    private static final String DEFAULT_RAW_REQUEST = "AAAAAAAAAA";
    private static final String UPDATED_RAW_REQUEST = "BBBBBBBBBB";

    private static final String DEFAULT_RAW_RESPONSE = "AAAAAAAAAA";
    private static final String UPDATED_RAW_RESPONSE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_PROCESSING = false;
    private static final Boolean UPDATED_PROCESSING = true;

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private AdyenPayoutErrorRepository adyenPayoutErrorRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restAdyenPayoutErrorMockMvc;

    private AdyenPayoutError adyenPayoutError;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AdyenPayoutErrorResource adyenPayoutErrorResource = new AdyenPayoutErrorResource(adyenPayoutErrorRepository);
        this.restAdyenPayoutErrorMockMvc = MockMvcBuilders.standaloneSetup(adyenPayoutErrorResource)
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
    public static AdyenPayoutError createEntity(EntityManager em) {
        AdyenPayoutError adyenPayoutError = new AdyenPayoutError()
            .rawRequest(DEFAULT_RAW_REQUEST)
            .rawResponse(DEFAULT_RAW_RESPONSE)
            .processing(DEFAULT_PROCESSING)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        return adyenPayoutError;
    }

    @Before
    public void initTest() {
        adyenPayoutError = createEntity(em);
    }

    @Test
    @Transactional
    public void createAdyenPayoutError() throws Exception {
        int databaseSizeBeforeCreate = adyenPayoutErrorRepository.findAll().size();

        // Create the AdyenPayoutError
        restAdyenPayoutErrorMockMvc.perform(post("/api/adyen-payout-errors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(adyenPayoutError)))
            .andExpect(status().isCreated());

        // Validate the AdyenPayoutError in the database
        List<AdyenPayoutError> adyenPayoutErrorList = adyenPayoutErrorRepository.findAll();
        assertThat(adyenPayoutErrorList).hasSize(databaseSizeBeforeCreate + 1);
        AdyenPayoutError testAdyenPayoutError = adyenPayoutErrorList.get(adyenPayoutErrorList.size() - 1);
        assertThat(testAdyenPayoutError.getRawRequest()).isEqualTo(DEFAULT_RAW_REQUEST);
        assertThat(testAdyenPayoutError.getRawResponse()).isEqualTo(DEFAULT_RAW_RESPONSE);
        assertThat(testAdyenPayoutError.isProcessing()).isEqualTo(DEFAULT_PROCESSING);
        assertThat(testAdyenPayoutError.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testAdyenPayoutError.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
    }

    @Test
    @Transactional
    public void createAdyenPayoutErrorWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = adyenPayoutErrorRepository.findAll().size();

        // Create the AdyenPayoutError with an existing ID
        adyenPayoutError.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAdyenPayoutErrorMockMvc.perform(post("/api/adyen-payout-errors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(adyenPayoutError)))
            .andExpect(status().isBadRequest());

        // Validate the AdyenPayoutError in the database
        List<AdyenPayoutError> adyenPayoutErrorList = adyenPayoutErrorRepository.findAll();
        assertThat(adyenPayoutErrorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllAdyenPayoutErrors() throws Exception {
        // Initialize the database
        adyenPayoutErrorRepository.saveAndFlush(adyenPayoutError);

        // Get all the adyenPayoutErrorList
        restAdyenPayoutErrorMockMvc.perform(get("/api/adyen-payout-errors?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(adyenPayoutError.getId().intValue())))
            .andExpect(jsonPath("$.[*].rawRequest").value(hasItem(DEFAULT_RAW_REQUEST.toString())))
            .andExpect(jsonPath("$.[*].rawResponse").value(hasItem(DEFAULT_RAW_RESPONSE.toString())))
            .andExpect(jsonPath("$.[*].processing").value(hasItem(DEFAULT_PROCESSING.booleanValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))));
    }

    @Test
    @Transactional
    public void getAdyenPayoutError() throws Exception {
        // Initialize the database
        adyenPayoutErrorRepository.saveAndFlush(adyenPayoutError);

        // Get the adyenPayoutError
        restAdyenPayoutErrorMockMvc.perform(get("/api/adyen-payout-errors/{id}", adyenPayoutError.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(adyenPayoutError.getId().intValue()))
            .andExpect(jsonPath("$.rawRequest").value(DEFAULT_RAW_REQUEST.toString()))
            .andExpect(jsonPath("$.rawResponse").value(DEFAULT_RAW_RESPONSE.toString()))
            .andExpect(jsonPath("$.processing").value(DEFAULT_PROCESSING.booleanValue()))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)));
    }

    @Test
    @Transactional
    public void getNonExistingAdyenPayoutError() throws Exception {
        // Get the adyenPayoutError
        restAdyenPayoutErrorMockMvc.perform(get("/api/adyen-payout-errors/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAdyenPayoutError() throws Exception {
        // Initialize the database
        adyenPayoutErrorRepository.saveAndFlush(adyenPayoutError);
        int databaseSizeBeforeUpdate = adyenPayoutErrorRepository.findAll().size();

        // Update the adyenPayoutError
        AdyenPayoutError updatedAdyenPayoutError = adyenPayoutErrorRepository.findOne(adyenPayoutError.getId());
        // Disconnect from session so that the updates on updatedAdyenPayoutError are not directly saved in db
        em.detach(updatedAdyenPayoutError);
        updatedAdyenPayoutError
            .rawRequest(UPDATED_RAW_REQUEST)
            .rawResponse(UPDATED_RAW_RESPONSE)
            .processing(UPDATED_PROCESSING)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restAdyenPayoutErrorMockMvc.perform(put("/api/adyen-payout-errors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAdyenPayoutError)))
            .andExpect(status().isOk());

        // Validate the AdyenPayoutError in the database
        List<AdyenPayoutError> adyenPayoutErrorList = adyenPayoutErrorRepository.findAll();
        assertThat(adyenPayoutErrorList).hasSize(databaseSizeBeforeUpdate);
        AdyenPayoutError testAdyenPayoutError = adyenPayoutErrorList.get(adyenPayoutErrorList.size() - 1);
        assertThat(testAdyenPayoutError.getRawRequest()).isEqualTo(UPDATED_RAW_REQUEST);
        assertThat(testAdyenPayoutError.getRawResponse()).isEqualTo(UPDATED_RAW_RESPONSE);
        assertThat(testAdyenPayoutError.isProcessing()).isEqualTo(UPDATED_PROCESSING);
        assertThat(testAdyenPayoutError.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testAdyenPayoutError.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    public void updateNonExistingAdyenPayoutError() throws Exception {
        int databaseSizeBeforeUpdate = adyenPayoutErrorRepository.findAll().size();

        // Create the AdyenPayoutError

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restAdyenPayoutErrorMockMvc.perform(put("/api/adyen-payout-errors")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(adyenPayoutError)))
            .andExpect(status().isCreated());

        // Validate the AdyenPayoutError in the database
        List<AdyenPayoutError> adyenPayoutErrorList = adyenPayoutErrorRepository.findAll();
        assertThat(adyenPayoutErrorList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteAdyenPayoutError() throws Exception {
        // Initialize the database
        adyenPayoutErrorRepository.saveAndFlush(adyenPayoutError);
        int databaseSizeBeforeDelete = adyenPayoutErrorRepository.findAll().size();

        // Get the adyenPayoutError
        restAdyenPayoutErrorMockMvc.perform(delete("/api/adyen-payout-errors/{id}", adyenPayoutError.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<AdyenPayoutError> adyenPayoutErrorList = adyenPayoutErrorRepository.findAll();
        assertThat(adyenPayoutErrorList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdyenPayoutError.class);
        AdyenPayoutError adyenPayoutError1 = new AdyenPayoutError();
        adyenPayoutError1.setId(1L);
        AdyenPayoutError adyenPayoutError2 = new AdyenPayoutError();
        adyenPayoutError2.setId(adyenPayoutError1.getId());
        assertThat(adyenPayoutError1).isEqualTo(adyenPayoutError2);
        adyenPayoutError2.setId(2L);
        assertThat(adyenPayoutError1).isNotEqualTo(adyenPayoutError2);
        adyenPayoutError1.setId(null);
        assertThat(adyenPayoutError1).isNotEqualTo(adyenPayoutError2);
    }
}
