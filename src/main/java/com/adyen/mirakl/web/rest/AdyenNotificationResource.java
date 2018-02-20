package com.adyen.mirakl.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.adyen.mirakl.domain.AdyenNotification;

import com.adyen.mirakl.repository.AdyenNotificationRepository;
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
 * REST controller for managing AdyenNotification.
 */
@RestController
@RequestMapping("/api")
public class AdyenNotificationResource {

    private final Logger log = LoggerFactory.getLogger(AdyenNotificationResource.class);

    private static final String ENTITY_NAME = "adyenNotification";

    private final AdyenNotificationRepository adyenNotificationRepository;

    public AdyenNotificationResource(AdyenNotificationRepository adyenNotificationRepository) {
        this.adyenNotificationRepository = adyenNotificationRepository;
    }

    /**
     * POST  /adyen-notifications : Create a new adyenNotification.
     *
     * @param adyenNotification the adyenNotification to create
     * @return the ResponseEntity with status 201 (Created) and with body the new adyenNotification, or with status 400 (Bad Request) if the adyenNotification has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/adyen-notifications")
    @Timed
    public ResponseEntity<AdyenNotification> createAdyenNotification(@RequestBody AdyenNotification adyenNotification) throws URISyntaxException {
        log.debug("REST request to save AdyenNotification : {}", adyenNotification);
        if (adyenNotification.getId() != null) {
            throw new BadRequestAlertException("A new adyenNotification cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AdyenNotification result = adyenNotificationRepository.save(adyenNotification);
        return ResponseEntity.created(new URI("/api/adyen-notifications/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /adyen-notifications : Updates an existing adyenNotification.
     *
     * @param adyenNotification the adyenNotification to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated adyenNotification,
     * or with status 400 (Bad Request) if the adyenNotification is not valid,
     * or with status 500 (Internal Server Error) if the adyenNotification couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/adyen-notifications")
    @Timed
    public ResponseEntity<AdyenNotification> updateAdyenNotification(@RequestBody AdyenNotification adyenNotification) throws URISyntaxException {
        log.debug("REST request to update AdyenNotification : {}", adyenNotification);
        if (adyenNotification.getId() == null) {
            return createAdyenNotification(adyenNotification);
        }
        AdyenNotification result = adyenNotificationRepository.save(adyenNotification);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, adyenNotification.getId().toString()))
            .body(result);
    }

    /**
     * GET  /adyen-notifications : get all the adyenNotifications.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of adyenNotifications in body
     */
    @GetMapping("/adyen-notifications")
    @Timed
    public List<AdyenNotification> getAllAdyenNotifications() {
        log.debug("REST request to get all AdyenNotifications");
        return adyenNotificationRepository.findAll();
        }

    /**
     * GET  /adyen-notifications/:id : get the "id" adyenNotification.
     *
     * @param id the id of the adyenNotification to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the adyenNotification, or with status 404 (Not Found)
     */
    @GetMapping("/adyen-notifications/{id}")
    @Timed
    public ResponseEntity<AdyenNotification> getAdyenNotification(@PathVariable Long id) {
        log.debug("REST request to get AdyenNotification : {}", id);
        AdyenNotification adyenNotification = adyenNotificationRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(adyenNotification));
    }

    /**
     * DELETE  /adyen-notifications/:id : delete the "id" adyenNotification.
     *
     * @param id the id of the adyenNotification to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/adyen-notifications/{id}")
    @Timed
    public ResponseEntity<Void> deleteAdyenNotification(@PathVariable Long id) {
        log.debug("REST request to delete AdyenNotification : {}", id);
        adyenNotificationRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
