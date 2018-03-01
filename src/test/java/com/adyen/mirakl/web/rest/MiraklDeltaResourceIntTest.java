package com.adyen.mirakl.web.rest;

import com.adyen.mirakl.AdyenMiraklConnectorApp;

import com.adyen.mirakl.domain.MiraklDelta;
import com.adyen.mirakl.repository.MiraklDeltaRepository;
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
 * Test class for the MiraklDeltaResource REST controller.
 *
 * @see MiraklDeltaResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class MiraklDeltaResourceIntTest {

    private static final ZonedDateTime DEFAULT_SHOP_DELTA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_SHOP_DELTA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private MiraklDeltaRepository miraklDeltaRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restMiraklDeltaMockMvc;

    private MiraklDelta miraklDelta;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final MiraklDeltaResource miraklDeltaResource = new MiraklDeltaResource(miraklDeltaRepository);
        this.restMiraklDeltaMockMvc = MockMvcBuilders.standaloneSetup(miraklDeltaResource)
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
    public static MiraklDelta createEntity(EntityManager em) {
        MiraklDelta miraklDelta = new MiraklDelta()
            .shopDelta(DEFAULT_SHOP_DELTA);
        return miraklDelta;
    }

    @Before
    public void initTest() {
        miraklDelta = createEntity(em);
    }

    @Test
    @Transactional
    public void createMiraklDelta() throws Exception {
        int databaseSizeBeforeCreate = miraklDeltaRepository.findAll().size();

        // Create the MiraklDelta
        restMiraklDeltaMockMvc.perform(post("/api/mirakl-deltas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(miraklDelta)))
            .andExpect(status().isCreated());

        // Validate the MiraklDelta in the database
        List<MiraklDelta> miraklDeltaList = miraklDeltaRepository.findAll();
        assertThat(miraklDeltaList).hasSize(databaseSizeBeforeCreate + 1);
        MiraklDelta testMiraklDelta = miraklDeltaList.get(miraklDeltaList.size() - 1);
        assertThat(testMiraklDelta.getShopDelta()).isEqualTo(DEFAULT_SHOP_DELTA);
    }

    @Test
    @Transactional
    public void createMiraklDeltaWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = miraklDeltaRepository.findAll().size();

        // Create the MiraklDelta with an existing ID
        miraklDelta.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMiraklDeltaMockMvc.perform(post("/api/mirakl-deltas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(miraklDelta)))
            .andExpect(status().isBadRequest());

        // Validate the MiraklDelta in the database
        List<MiraklDelta> miraklDeltaList = miraklDeltaRepository.findAll();
        assertThat(miraklDeltaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllMiraklDeltas() throws Exception {
        // Initialize the database
        miraklDeltaRepository.saveAndFlush(miraklDelta);

        // Get all the miraklDeltaList
        restMiraklDeltaMockMvc.perform(get("/api/mirakl-deltas?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(miraklDelta.getId().intValue())))
            .andExpect(jsonPath("$.[*].shopDelta").value(hasItem(sameInstant(DEFAULT_SHOP_DELTA))));
    }

    @Test
    @Transactional
    public void getMiraklDelta() throws Exception {
        // Initialize the database
        miraklDeltaRepository.saveAndFlush(miraklDelta);

        // Get the miraklDelta
        restMiraklDeltaMockMvc.perform(get("/api/mirakl-deltas/{id}", miraklDelta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(miraklDelta.getId().intValue()))
            .andExpect(jsonPath("$.shopDelta").value(sameInstant(DEFAULT_SHOP_DELTA)));
    }

    @Test
    @Transactional
    public void getNonExistingMiraklDelta() throws Exception {
        // Get the miraklDelta
        restMiraklDeltaMockMvc.perform(get("/api/mirakl-deltas/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMiraklDelta() throws Exception {
        // Initialize the database
        miraklDeltaRepository.saveAndFlush(miraklDelta);
        int databaseSizeBeforeUpdate = miraklDeltaRepository.findAll().size();

        // Update the miraklDelta
        MiraklDelta updatedMiraklDelta = miraklDeltaRepository.findOne(miraklDelta.getId());
        // Disconnect from session so that the updates on updatedMiraklDelta are not directly saved in db
        em.detach(updatedMiraklDelta);
        updatedMiraklDelta
            .shopDelta(UPDATED_SHOP_DELTA);

        restMiraklDeltaMockMvc.perform(put("/api/mirakl-deltas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedMiraklDelta)))
            .andExpect(status().isOk());

        // Validate the MiraklDelta in the database
        List<MiraklDelta> miraklDeltaList = miraklDeltaRepository.findAll();
        assertThat(miraklDeltaList).hasSize(databaseSizeBeforeUpdate);
        MiraklDelta testMiraklDelta = miraklDeltaList.get(miraklDeltaList.size() - 1);
        assertThat(testMiraklDelta.getShopDelta()).isEqualTo(UPDATED_SHOP_DELTA);
    }

    @Test
    @Transactional
    public void updateNonExistingMiraklDelta() throws Exception {
        int databaseSizeBeforeUpdate = miraklDeltaRepository.findAll().size();

        // Create the MiraklDelta

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restMiraklDeltaMockMvc.perform(put("/api/mirakl-deltas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(miraklDelta)))
            .andExpect(status().isCreated());

        // Validate the MiraklDelta in the database
        List<MiraklDelta> miraklDeltaList = miraklDeltaRepository.findAll();
        assertThat(miraklDeltaList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteMiraklDelta() throws Exception {
        // Initialize the database
        miraklDeltaRepository.saveAndFlush(miraklDelta);
        int databaseSizeBeforeDelete = miraklDeltaRepository.findAll().size();

        // Get the miraklDelta
        restMiraklDeltaMockMvc.perform(delete("/api/mirakl-deltas/{id}", miraklDelta.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<MiraklDelta> miraklDeltaList = miraklDeltaRepository.findAll();
        assertThat(miraklDeltaList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MiraklDelta.class);
        MiraklDelta miraklDelta1 = new MiraklDelta();
        miraklDelta1.setId(1L);
        MiraklDelta miraklDelta2 = new MiraklDelta();
        miraklDelta2.setId(miraklDelta1.getId());
        assertThat(miraklDelta1).isEqualTo(miraklDelta2);
        miraklDelta2.setId(2L);
        assertThat(miraklDelta1).isNotEqualTo(miraklDelta2);
        miraklDelta1.setId(null);
        assertThat(miraklDelta1).isNotEqualTo(miraklDelta2);
    }
}
