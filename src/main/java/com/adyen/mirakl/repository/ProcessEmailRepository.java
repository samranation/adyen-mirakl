package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.ProcessEmail;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ProcessEmail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProcessEmailRepository extends JpaRepository<ProcessEmail, Long> {

}
