package com.adyen.mirakl.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.adyen.mirakl.domain.AdyenPayoutError;

import com.adyen.mirakl.repository.AdyenPayoutErrorRepository;
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
 * REST controller for managing AdyenPayoutError.
 */
@RestController
@RequestMapping("/api")
public class AdyenPayoutErrorResource {

    private final Logger log = LoggerFactory.getLogger(AdyenPayoutErrorResource.class);

    private static final String ENTITY_NAME = "adyenPayoutError";

    private final AdyenPayoutErrorRepository adyenPayoutErrorRepository;

    public AdyenPayoutErrorResource(AdyenPayoutErrorRepository adyenPayoutErrorRepository) {
        this.adyenPayoutErrorRepository = adyenPayoutErrorRepository;
    }

    /**
     * POST  /adyen-payout-errors : Create a new adyenPayoutError.
     *
     * @param adyenPayoutError the adyenPayoutError to create
     * @return the ResponseEntity with status 201 (Created) and with body the new adyenPayoutError, or with status 400 (Bad Request) if the adyenPayoutError has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/adyen-payout-errors")
    @Timed
    public ResponseEntity<AdyenPayoutError> createAdyenPayoutError(@RequestBody AdyenPayoutError adyenPayoutError) throws URISyntaxException {
        log.debug("REST request to save AdyenPayoutError : {}", adyenPayoutError);
        if (adyenPayoutError.getId() != null) {
            throw new BadRequestAlertException("A new adyenPayoutError cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AdyenPayoutError result = adyenPayoutErrorRepository.save(adyenPayoutError);
        return ResponseEntity.created(new URI("/api/adyen-payout-errors/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /adyen-payout-errors : Updates an existing adyenPayoutError.
     *
     * @param adyenPayoutError the adyenPayoutError to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated adyenPayoutError,
     * or with status 400 (Bad Request) if the adyenPayoutError is not valid,
     * or with status 500 (Internal Server Error) if the adyenPayoutError couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/adyen-payout-errors")
    @Timed
    public ResponseEntity<AdyenPayoutError> updateAdyenPayoutError(@RequestBody AdyenPayoutError adyenPayoutError) throws URISyntaxException {
        log.debug("REST request to update AdyenPayoutError : {}", adyenPayoutError);
        if (adyenPayoutError.getId() == null) {
            return createAdyenPayoutError(adyenPayoutError);
        }
        AdyenPayoutError result = adyenPayoutErrorRepository.save(adyenPayoutError);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, adyenPayoutError.getId().toString()))
            .body(result);
    }

    /**
     * GET  /adyen-payout-errors : get all the adyenPayoutErrors.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of adyenPayoutErrors in body
     */
    @GetMapping("/adyen-payout-errors")
    @Timed
    public List<AdyenPayoutError> getAllAdyenPayoutErrors() {
        log.debug("REST request to get all AdyenPayoutErrors");
        return adyenPayoutErrorRepository.findAll();
        }

    /**
     * GET  /adyen-payout-errors/:id : get the "id" adyenPayoutError.
     *
     * @param id the id of the adyenPayoutError to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the adyenPayoutError, or with status 404 (Not Found)
     */
    @GetMapping("/adyen-payout-errors/{id}")
    @Timed
    public ResponseEntity<AdyenPayoutError> getAdyenPayoutError(@PathVariable Long id) {
        log.debug("REST request to get AdyenPayoutError : {}", id);
        AdyenPayoutError adyenPayoutError = adyenPayoutErrorRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(adyenPayoutError));
    }

    /**
     * DELETE  /adyen-payout-errors/:id : delete the "id" adyenPayoutError.
     *
     * @param id the id of the adyenPayoutError to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/adyen-payout-errors/{id}")
    @Timed
    public ResponseEntity<Void> deleteAdyenPayoutError(@PathVariable Long id) {
        log.debug("REST request to delete AdyenPayoutError : {}", id);
        adyenPayoutErrorRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
