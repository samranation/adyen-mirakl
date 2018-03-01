package com.adyen.mirakl.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.adyen.mirakl.domain.MiraklDelta;

import com.adyen.mirakl.repository.MiraklDeltaRepository;
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
 * REST controller for managing MiraklDelta.
 */
@RestController
@RequestMapping("/api")
public class MiraklDeltaResource {

    private final Logger log = LoggerFactory.getLogger(MiraklDeltaResource.class);

    private static final String ENTITY_NAME = "miraklDelta";

    private final MiraklDeltaRepository miraklDeltaRepository;

    public MiraklDeltaResource(MiraklDeltaRepository miraklDeltaRepository) {
        this.miraklDeltaRepository = miraklDeltaRepository;
    }

    /**
     * POST  /mirakl-deltas : Create a new miraklDelta.
     *
     * @param miraklDelta the miraklDelta to create
     * @return the ResponseEntity with status 201 (Created) and with body the new miraklDelta, or with status 400 (Bad Request) if the miraklDelta has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/mirakl-deltas")
    @Timed
    public ResponseEntity<MiraklDelta> createMiraklDelta(@RequestBody MiraklDelta miraklDelta) throws URISyntaxException {
        log.debug("REST request to save MiraklDelta : {}", miraklDelta);
        if (miraklDelta.getId() != null) {
            throw new BadRequestAlertException("A new miraklDelta cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MiraklDelta result = miraklDeltaRepository.save(miraklDelta);
        return ResponseEntity.created(new URI("/api/mirakl-deltas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /mirakl-deltas : Updates an existing miraklDelta.
     *
     * @param miraklDelta the miraklDelta to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated miraklDelta,
     * or with status 400 (Bad Request) if the miraklDelta is not valid,
     * or with status 500 (Internal Server Error) if the miraklDelta couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/mirakl-deltas")
    @Timed
    public ResponseEntity<MiraklDelta> updateMiraklDelta(@RequestBody MiraklDelta miraklDelta) throws URISyntaxException {
        log.debug("REST request to update MiraklDelta : {}", miraklDelta);
        if (miraklDelta.getId() == null) {
            return createMiraklDelta(miraklDelta);
        }
        MiraklDelta result = miraklDeltaRepository.save(miraklDelta);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, miraklDelta.getId().toString()))
            .body(result);
    }

    /**
     * GET  /mirakl-deltas : get all the miraklDeltas.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of miraklDeltas in body
     */
    @GetMapping("/mirakl-deltas")
    @Timed
    public List<MiraklDelta> getAllMiraklDeltas() {
        log.debug("REST request to get all MiraklDeltas");
        return miraklDeltaRepository.findAll();
        }

    /**
     * GET  /mirakl-deltas/:id : get the "id" miraklDelta.
     *
     * @param id the id of the miraklDelta to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the miraklDelta, or with status 404 (Not Found)
     */
    @GetMapping("/mirakl-deltas/{id}")
    @Timed
    public ResponseEntity<MiraklDelta> getMiraklDelta(@PathVariable Long id) {
        log.debug("REST request to get MiraklDelta : {}", id);
        MiraklDelta miraklDelta = miraklDeltaRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(miraklDelta));
    }

    /**
     * DELETE  /mirakl-deltas/:id : delete the "id" miraklDelta.
     *
     * @param id the id of the miraklDelta to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/mirakl-deltas/{id}")
    @Timed
    public ResponseEntity<Void> deleteMiraklDelta(@PathVariable Long id) {
        log.debug("REST request to delete MiraklDelta : {}", id);
        miraklDeltaRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
