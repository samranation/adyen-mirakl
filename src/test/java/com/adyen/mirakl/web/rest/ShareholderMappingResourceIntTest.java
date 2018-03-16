package com.adyen.mirakl.web.rest;

import com.adyen.mirakl.AdyenMiraklConnectorApp;

import com.adyen.mirakl.domain.ShareholderMapping;
import com.adyen.mirakl.repository.ShareholderMappingRepository;
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

import javax.persistence.EntityManager;
import java.util.List;

import static com.adyen.mirakl.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ShareholderMappingResource REST controller.
 *
 * @see ShareholderMappingResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class ShareholderMappingResourceIntTest {

    private static final String DEFAULT_MIRAKL_SHOP_ID = "AAAAAAAAAA";
    private static final String UPDATED_MIRAKL_SHOP_ID = "BBBBBBBBBB";

    private static final Integer DEFAULT_MIRAKL_UBO_NUMBER = 1;
    private static final Integer UPDATED_MIRAKL_UBO_NUMBER = 2;

    private static final String DEFAULT_ADYEN_SHAREHOLDER_CODE = "AAAAAAAAAA";
    private static final String UPDATED_ADYEN_SHAREHOLDER_CODE = "BBBBBBBBBB";

    @Autowired
    private ShareholderMappingRepository shareholderMappingRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restShareholderMappingMockMvc;

    private ShareholderMapping shareholderMapping;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ShareholderMappingResource shareholderMappingResource = new ShareholderMappingResource(shareholderMappingRepository);
        this.restShareholderMappingMockMvc = MockMvcBuilders.standaloneSetup(shareholderMappingResource)
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
    public static ShareholderMapping createEntity(EntityManager em) {
        ShareholderMapping shareholderMapping = new ShareholderMapping()
            .miraklShopId(DEFAULT_MIRAKL_SHOP_ID)
            .miraklUboNumber(DEFAULT_MIRAKL_UBO_NUMBER)
            .adyenShareholderCode(DEFAULT_ADYEN_SHAREHOLDER_CODE);
        return shareholderMapping;
    }

    @Before
    public void initTest() {
        shareholderMapping = createEntity(em);
    }

    @Test
    @Transactional
    public void createShareholderMapping() throws Exception {
        int databaseSizeBeforeCreate = shareholderMappingRepository.findAll().size();

        // Create the ShareholderMapping
        restShareholderMappingMockMvc.perform(post("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shareholderMapping)))
            .andExpect(status().isCreated());

        // Validate the ShareholderMapping in the database
        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeCreate + 1);
        ShareholderMapping testShareholderMapping = shareholderMappingList.get(shareholderMappingList.size() - 1);
        assertThat(testShareholderMapping.getMiraklShopId()).isEqualTo(DEFAULT_MIRAKL_SHOP_ID);
        assertThat(testShareholderMapping.getMiraklUboNumber()).isEqualTo(DEFAULT_MIRAKL_UBO_NUMBER);
        assertThat(testShareholderMapping.getAdyenShareholderCode()).isEqualTo(DEFAULT_ADYEN_SHAREHOLDER_CODE);
    }

    @Test
    @Transactional
    public void createShareholderMappingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = shareholderMappingRepository.findAll().size();

        // Create the ShareholderMapping with an existing ID
        shareholderMapping.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restShareholderMappingMockMvc.perform(post("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shareholderMapping)))
            .andExpect(status().isBadRequest());

        // Validate the ShareholderMapping in the database
        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkMiraklShopIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = shareholderMappingRepository.findAll().size();
        // set the field null
        shareholderMapping.setMiraklShopId(null);

        // Create the ShareholderMapping, which fails.

        restShareholderMappingMockMvc.perform(post("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shareholderMapping)))
            .andExpect(status().isBadRequest());

        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMiraklUboNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = shareholderMappingRepository.findAll().size();
        // set the field null
        shareholderMapping.setMiraklUboNumber(null);

        // Create the ShareholderMapping, which fails.

        restShareholderMappingMockMvc.perform(post("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shareholderMapping)))
            .andExpect(status().isBadRequest());

        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkAdyenShareholderCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = shareholderMappingRepository.findAll().size();
        // set the field null
        shareholderMapping.setAdyenShareholderCode(null);

        // Create the ShareholderMapping, which fails.

        restShareholderMappingMockMvc.perform(post("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shareholderMapping)))
            .andExpect(status().isBadRequest());

        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllShareholderMappings() throws Exception {
        // Initialize the database
        shareholderMappingRepository.saveAndFlush(shareholderMapping);

        // Get all the shareholderMappingList
        restShareholderMappingMockMvc.perform(get("/api/shareholder-mappings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shareholderMapping.getId().intValue())))
            .andExpect(jsonPath("$.[*].miraklShopId").value(hasItem(DEFAULT_MIRAKL_SHOP_ID.toString())))
            .andExpect(jsonPath("$.[*].miraklUboNumber").value(hasItem(DEFAULT_MIRAKL_UBO_NUMBER)))
            .andExpect(jsonPath("$.[*].adyenShareholderCode").value(hasItem(DEFAULT_ADYEN_SHAREHOLDER_CODE.toString())));
    }

    @Test
    @Transactional
    public void getShareholderMapping() throws Exception {
        // Initialize the database
        shareholderMappingRepository.saveAndFlush(shareholderMapping);

        // Get the shareholderMapping
        restShareholderMappingMockMvc.perform(get("/api/shareholder-mappings/{id}", shareholderMapping.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(shareholderMapping.getId().intValue()))
            .andExpect(jsonPath("$.miraklShopId").value(DEFAULT_MIRAKL_SHOP_ID.toString()))
            .andExpect(jsonPath("$.miraklUboNumber").value(DEFAULT_MIRAKL_UBO_NUMBER))
            .andExpect(jsonPath("$.adyenShareholderCode").value(DEFAULT_ADYEN_SHAREHOLDER_CODE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingShareholderMapping() throws Exception {
        // Get the shareholderMapping
        restShareholderMappingMockMvc.perform(get("/api/shareholder-mappings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateShareholderMapping() throws Exception {
        // Initialize the database
        shareholderMappingRepository.saveAndFlush(shareholderMapping);
        int databaseSizeBeforeUpdate = shareholderMappingRepository.findAll().size();

        // Update the shareholderMapping
        ShareholderMapping updatedShareholderMapping = shareholderMappingRepository.findOne(shareholderMapping.getId());
        // Disconnect from session so that the updates on updatedShareholderMapping are not directly saved in db
        em.detach(updatedShareholderMapping);
        updatedShareholderMapping
            .miraklShopId(UPDATED_MIRAKL_SHOP_ID)
            .miraklUboNumber(UPDATED_MIRAKL_UBO_NUMBER)
            .adyenShareholderCode(UPDATED_ADYEN_SHAREHOLDER_CODE);

        restShareholderMappingMockMvc.perform(put("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedShareholderMapping)))
            .andExpect(status().isOk());

        // Validate the ShareholderMapping in the database
        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeUpdate);
        ShareholderMapping testShareholderMapping = shareholderMappingList.get(shareholderMappingList.size() - 1);
        assertThat(testShareholderMapping.getMiraklShopId()).isEqualTo(UPDATED_MIRAKL_SHOP_ID);
        assertThat(testShareholderMapping.getMiraklUboNumber()).isEqualTo(UPDATED_MIRAKL_UBO_NUMBER);
        assertThat(testShareholderMapping.getAdyenShareholderCode()).isEqualTo(UPDATED_ADYEN_SHAREHOLDER_CODE);
    }

    @Test
    @Transactional
    public void updateNonExistingShareholderMapping() throws Exception {
        int databaseSizeBeforeUpdate = shareholderMappingRepository.findAll().size();

        // Create the ShareholderMapping

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restShareholderMappingMockMvc.perform(put("/api/shareholder-mappings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(shareholderMapping)))
            .andExpect(status().isCreated());

        // Validate the ShareholderMapping in the database
        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteShareholderMapping() throws Exception {
        // Initialize the database
        shareholderMappingRepository.saveAndFlush(shareholderMapping);
        int databaseSizeBeforeDelete = shareholderMappingRepository.findAll().size();

        // Get the shareholderMapping
        restShareholderMappingMockMvc.perform(delete("/api/shareholder-mappings/{id}", shareholderMapping.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ShareholderMapping> shareholderMappingList = shareholderMappingRepository.findAll();
        assertThat(shareholderMappingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShareholderMapping.class);
        ShareholderMapping shareholderMapping1 = new ShareholderMapping();
        shareholderMapping1.setId(1L);
        ShareholderMapping shareholderMapping2 = new ShareholderMapping();
        shareholderMapping2.setId(shareholderMapping1.getId());
        assertThat(shareholderMapping1).isEqualTo(shareholderMapping2);
        shareholderMapping2.setId(2L);
        assertThat(shareholderMapping1).isNotEqualTo(shareholderMapping2);
        shareholderMapping1.setId(null);
        assertThat(shareholderMapping1).isNotEqualTo(shareholderMapping2);
    }
}
