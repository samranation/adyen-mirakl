package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.ProcessEmail;
import com.adyen.mirakl.domain.enumeration.EmailState;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA repository for the ProcessEmail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProcessEmailRepository extends JpaRepository<ProcessEmail, Long> {

    Optional<ProcessEmail> findOneByEmailIdentifier(String emailIdentifier);

    List<ProcessEmail> findByState(EmailState emailState);

}
