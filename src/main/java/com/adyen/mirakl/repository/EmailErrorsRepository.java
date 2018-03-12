package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.EmailError;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the EmailErrors entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EmailErrorsRepository extends JpaRepository<EmailError, Long> {

}
