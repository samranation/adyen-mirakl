package com.adyen.mirakl.web.rest;

import com.adyen.mirakl.AdyenMiraklConnectorApp;

import com.adyen.mirakl.domain.ProcessEmail;
import com.adyen.mirakl.repository.ProcessEmailRepository;
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
 * Test class for the ProcessEmailResource REST controller.
 *
 * @see ProcessEmailResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class ProcessEmailResourceIntTest {

    private static final String DEFAULT_TO = "AAAAAAAAAA";
    private static final String UPDATED_TO = "BBBBBBBBBB";

    private static final String DEFAULT_SUBJECT = "AAAAAAAAAA";
    private static final String UPDATED_SUBJECT = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_MULTIPART = false;
    private static final Boolean UPDATED_IS_MULTIPART = true;

    private static final Boolean DEFAULT_IS_HTML = false;
    private static final Boolean UPDATED_IS_HTML = true;

    @Autowired
    private ProcessEmailRepository processEmailRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restProcessEmailMockMvc;

    private ProcessEmail processEmail;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ProcessEmailResource processEmailResource = new ProcessEmailResource(processEmailRepository);
        this.restProcessEmailMockMvc = MockMvcBuilders.standaloneSetup(processEmailResource)
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
    public static ProcessEmail createEntity(EntityManager em) {
        ProcessEmail processEmail = new ProcessEmail()
            .to(DEFAULT_TO)
            .subject(DEFAULT_SUBJECT)
            .content(DEFAULT_CONTENT)
            .isMultipart(DEFAULT_IS_MULTIPART)
            .isHtml(DEFAULT_IS_HTML);
        return processEmail;
    }

    @Before
    public void initTest() {
        processEmail = createEntity(em);
    }

    @Test
    @Transactional
    public void createProcessEmail() throws Exception {
        int databaseSizeBeforeCreate = processEmailRepository.findAll().size();

        // Create the ProcessEmail
        restProcessEmailMockMvc.perform(post("/api/process-emails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(processEmail)))
            .andExpect(status().isCreated());

        // Validate the ProcessEmail in the database
        List<ProcessEmail> processEmailList = processEmailRepository.findAll();
        assertThat(processEmailList).hasSize(databaseSizeBeforeCreate + 1);
        ProcessEmail testProcessEmail = processEmailList.get(processEmailList.size() - 1);
        assertThat(testProcessEmail.getTo()).isEqualTo(DEFAULT_TO);
        assertThat(testProcessEmail.getSubject()).isEqualTo(DEFAULT_SUBJECT);
        assertThat(testProcessEmail.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testProcessEmail.isIsMultipart()).isEqualTo(DEFAULT_IS_MULTIPART);
        assertThat(testProcessEmail.isIsHtml()).isEqualTo(DEFAULT_IS_HTML);
    }

    @Test
    @Transactional
    public void createProcessEmailWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = processEmailRepository.findAll().size();

        // Create the ProcessEmail with an existing ID
        processEmail.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProcessEmailMockMvc.perform(post("/api/process-emails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(processEmail)))
            .andExpect(status().isBadRequest());

        // Validate the ProcessEmail in the database
        List<ProcessEmail> processEmailList = processEmailRepository.findAll();
        assertThat(processEmailList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllProcessEmails() throws Exception {
        // Initialize the database
        processEmailRepository.saveAndFlush(processEmail);

        // Get all the processEmailList
        restProcessEmailMockMvc.perform(get("/api/process-emails?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(processEmail.getId().intValue())))
            .andExpect(jsonPath("$.[*].to").value(hasItem(DEFAULT_TO.toString())))
            .andExpect(jsonPath("$.[*].subject").value(hasItem(DEFAULT_SUBJECT.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].isMultipart").value(hasItem(DEFAULT_IS_MULTIPART.booleanValue())))
            .andExpect(jsonPath("$.[*].isHtml").value(hasItem(DEFAULT_IS_HTML.booleanValue())));
    }

    @Test
    @Transactional
    public void getProcessEmail() throws Exception {
        // Initialize the database
        processEmailRepository.saveAndFlush(processEmail);

        // Get the processEmail
        restProcessEmailMockMvc.perform(get("/api/process-emails/{id}", processEmail.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(processEmail.getId().intValue()))
            .andExpect(jsonPath("$.to").value(DEFAULT_TO.toString()))
            .andExpect(jsonPath("$.subject").value(DEFAULT_SUBJECT.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.isMultipart").value(DEFAULT_IS_MULTIPART.booleanValue()))
            .andExpect(jsonPath("$.isHtml").value(DEFAULT_IS_HTML.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingProcessEmail() throws Exception {
        // Get the processEmail
        restProcessEmailMockMvc.perform(get("/api/process-emails/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProcessEmail() throws Exception {
        // Initialize the database
        processEmailRepository.saveAndFlush(processEmail);
        int databaseSizeBeforeUpdate = processEmailRepository.findAll().size();

        // Update the processEmail
        ProcessEmail updatedProcessEmail = processEmailRepository.findOne(processEmail.getId());
        // Disconnect from session so that the updates on updatedProcessEmail are not directly saved in db
        em.detach(updatedProcessEmail);
        updatedProcessEmail
            .to(UPDATED_TO)
            .subject(UPDATED_SUBJECT)
            .content(UPDATED_CONTENT)
            .isMultipart(UPDATED_IS_MULTIPART)
            .isHtml(UPDATED_IS_HTML);

        restProcessEmailMockMvc.perform(put("/api/process-emails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedProcessEmail)))
            .andExpect(status().isOk());

        // Validate the ProcessEmail in the database
        List<ProcessEmail> processEmailList = processEmailRepository.findAll();
        assertThat(processEmailList).hasSize(databaseSizeBeforeUpdate);
        ProcessEmail testProcessEmail = processEmailList.get(processEmailList.size() - 1);
        assertThat(testProcessEmail.getTo()).isEqualTo(UPDATED_TO);
        assertThat(testProcessEmail.getSubject()).isEqualTo(UPDATED_SUBJECT);
        assertThat(testProcessEmail.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testProcessEmail.isIsMultipart()).isEqualTo(UPDATED_IS_MULTIPART);
        assertThat(testProcessEmail.isIsHtml()).isEqualTo(UPDATED_IS_HTML);
    }

    @Test
    @Transactional
    public void updateNonExistingProcessEmail() throws Exception {
        int databaseSizeBeforeUpdate = processEmailRepository.findAll().size();

        // Create the ProcessEmail

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restProcessEmailMockMvc.perform(put("/api/process-emails")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(processEmail)))
            .andExpect(status().isCreated());

        // Validate the ProcessEmail in the database
        List<ProcessEmail> processEmailList = processEmailRepository.findAll();
        assertThat(processEmailList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteProcessEmail() throws Exception {
        // Initialize the database
        processEmailRepository.saveAndFlush(processEmail);
        int databaseSizeBeforeDelete = processEmailRepository.findAll().size();

        // Get the processEmail
        restProcessEmailMockMvc.perform(delete("/api/process-emails/{id}", processEmail.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ProcessEmail> processEmailList = processEmailRepository.findAll();
        assertThat(processEmailList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProcessEmail.class);
        ProcessEmail processEmail1 = new ProcessEmail();
        processEmail1.setId(1L);
        ProcessEmail processEmail2 = new ProcessEmail();
        processEmail2.setId(processEmail1.getId());
        assertThat(processEmail1).isEqualTo(processEmail2);
        processEmail2.setId(2L);
        assertThat(processEmail1).isNotEqualTo(processEmail2);
        processEmail1.setId(null);
        assertThat(processEmail1).isNotEqualTo(processEmail2);
    }
}
