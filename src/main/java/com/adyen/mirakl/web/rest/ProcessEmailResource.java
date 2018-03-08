package com.adyen.mirakl.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.adyen.mirakl.domain.ProcessEmail;

import com.adyen.mirakl.repository.ProcessEmailRepository;
import com.adyen.mirakl.web.rest.errors.BadRequestAlertException;
import com.adyen.mirakl.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ProcessEmail.
 */
@RestController
@RequestMapping("/api")
public class ProcessEmailResource {

    private final Logger log = LoggerFactory.getLogger(ProcessEmailResource.class);

    private static final String ENTITY_NAME = "processEmail";

    private final ProcessEmailRepository processEmailRepository;

    public ProcessEmailResource(ProcessEmailRepository processEmailRepository) {
        this.processEmailRepository = processEmailRepository;
    }

    /**
     * POST  /process-emails : Create a new processEmail.
     *
     * @param processEmail the processEmail to create
     * @return the ResponseEntity with status 201 (Created) and with body the new processEmail, or with status 400 (Bad Request) if the processEmail has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/process-emails")
    @Timed
    public ResponseEntity<ProcessEmail> createProcessEmail(@RequestBody ProcessEmail processEmail) throws URISyntaxException {
        log.debug("REST request to save ProcessEmail : {}", processEmail);
        if (processEmail.getId() != null) {
            throw new BadRequestAlertException("A new processEmail cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProcessEmail result = processEmailRepository.save(processEmail);
        return ResponseEntity.created(new URI("/api/process-emails/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /process-emails : Updates an existing processEmail.
     *
     * @param processEmail the processEmail to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated processEmail,
     * or with status 400 (Bad Request) if the processEmail is not valid,
     * or with status 500 (Internal Server Error) if the processEmail couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/process-emails")
    @Timed
    public ResponseEntity<ProcessEmail> updateProcessEmail(@RequestBody ProcessEmail processEmail) throws URISyntaxException {
        log.debug("REST request to update ProcessEmail : {}", processEmail);
        if (processEmail.getId() == null) {
            return createProcessEmail(processEmail);
        }
        ProcessEmail result = processEmailRepository.save(processEmail);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, processEmail.getId().toString()))
            .body(result);
    }

    /**
     * GET  /process-emails : get all the processEmails.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of processEmails in body
     */
    @GetMapping("/process-emails")
    @Timed
    public List<ProcessEmail> getAllProcessEmails() {
        log.debug("REST request to get all ProcessEmails");
        return processEmailRepository.findAll();
        }

    /**
     * GET  /process-emails/:id : get the "id" processEmail.
     *
     * @param id the id of the processEmail to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the processEmail, or with status 404 (Not Found)
     */
    @GetMapping("/process-emails/{id}")
    @Timed
    public ResponseEntity<ProcessEmail> getProcessEmail(@PathVariable Long id) {
        log.debug("REST request to get ProcessEmail : {}", id);
        ProcessEmail processEmail = processEmailRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(processEmail));
    }

    /**
     * DELETE  /process-emails/:id : delete the "id" processEmail.
     *
     * @param id the id of the processEmail to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/process-emails/{id}")
    @Timed
    public ResponseEntity<Void> deleteProcessEmail(@PathVariable Long id) {
        log.debug("REST request to delete ProcessEmail : {}", id);
        processEmailRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
