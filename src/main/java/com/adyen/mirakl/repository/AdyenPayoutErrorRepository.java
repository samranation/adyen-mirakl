package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.AdyenPayoutError;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the AdyenPayoutError entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AdyenPayoutErrorRepository extends JpaRepository<AdyenPayoutError, Long> {

}
