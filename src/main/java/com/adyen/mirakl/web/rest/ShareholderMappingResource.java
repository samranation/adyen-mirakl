package com.adyen.mirakl.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.adyen.mirakl.domain.ShareholderMapping;

import com.adyen.mirakl.repository.ShareholderMappingRepository;
import com.adyen.mirakl.web.rest.errors.BadRequestAlertException;
import com.adyen.mirakl.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ShareholderMapping.
 */
@RestController
@RequestMapping("/api")
public class ShareholderMappingResource {

    private final Logger log = LoggerFactory.getLogger(ShareholderMappingResource.class);

    private static final String ENTITY_NAME = "shareholderMapping";

    private final ShareholderMappingRepository shareholderMappingRepository;

    public ShareholderMappingResource(ShareholderMappingRepository shareholderMappingRepository) {
        this.shareholderMappingRepository = shareholderMappingRepository;
    }

    /**
     * POST  /shareholder-mappings : Create a new shareholderMapping.
     *
     * @param shareholderMapping the shareholderMapping to create
     * @return the ResponseEntity with status 201 (Created) and with body the new shareholderMapping, or with status 400 (Bad Request) if the shareholderMapping has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/shareholder-mappings")
    @Timed
    public ResponseEntity<ShareholderMapping> createShareholderMapping(@Valid @RequestBody ShareholderMapping shareholderMapping) throws URISyntaxException {
        log.debug("REST request to save ShareholderMapping : {}", shareholderMapping);
        if (shareholderMapping.getId() != null) {
            throw new BadRequestAlertException("A new shareholderMapping cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ShareholderMapping result = shareholderMappingRepository.save(shareholderMapping);
        return ResponseEntity.created(new URI("/api/shareholder-mappings/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /shareholder-mappings : Updates an existing shareholderMapping.
     *
     * @param shareholderMapping the shareholderMapping to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated shareholderMapping,
     * or with status 400 (Bad Request) if the shareholderMapping is not valid,
     * or with status 500 (Internal Server Error) if the shareholderMapping couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/shareholder-mappings")
    @Timed
    public ResponseEntity<ShareholderMapping> updateShareholderMapping(@Valid @RequestBody ShareholderMapping shareholderMapping) throws URISyntaxException {
        log.debug("REST request to update ShareholderMapping : {}", shareholderMapping);
        if (shareholderMapping.getId() == null) {
            return createShareholderMapping(shareholderMapping);
        }
        ShareholderMapping result = shareholderMappingRepository.save(shareholderMapping);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, shareholderMapping.getId().toString()))
            .body(result);
    }

    /**
     * GET  /shareholder-mappings : get all the shareholderMappings.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of shareholderMappings in body
     */
    @GetMapping("/shareholder-mappings")
    @Timed
    public List<ShareholderMapping> getAllShareholderMappings() {
        log.debug("REST request to get all ShareholderMappings");
        return shareholderMappingRepository.findAll();
        }

    /**
     * GET  /shareholder-mappings/:id : get the "id" shareholderMapping.
     *
     * @param id the id of the shareholderMapping to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the shareholderMapping, or with status 404 (Not Found)
     */
    @GetMapping("/shareholder-mappings/{id}")
    @Timed
    public ResponseEntity<ShareholderMapping> getShareholderMapping(@PathVariable Long id) {
        log.debug("REST request to get ShareholderMapping : {}", id);
        ShareholderMapping shareholderMapping = shareholderMappingRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(shareholderMapping));
    }

    /**
     * DELETE  /shareholder-mappings/:id : delete the "id" shareholderMapping.
     *
     * @param id the id of the shareholderMapping to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/shareholder-mappings/{id}")
    @Timed
    public ResponseEntity<Void> deleteShareholderMapping(@PathVariable Long id) {
        log.debug("REST request to delete ShareholderMapping : {}", id);
        shareholderMappingRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
