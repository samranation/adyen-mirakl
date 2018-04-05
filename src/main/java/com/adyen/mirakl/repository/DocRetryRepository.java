package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.DocRetry;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the DocRetry entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DocRetryRepository extends JpaRepository<DocRetry, Long> {

}
