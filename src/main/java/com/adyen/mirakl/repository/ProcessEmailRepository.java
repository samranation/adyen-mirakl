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

    @Query("select e from ProcessEmail as e left join fetch e.emailErrors where e.emailIdentifier = ?1")
    Optional<ProcessEmail> findOneByEmailIdentifier(String emailIdentifier);

    @Query("select e from ProcessEmail as e left join fetch e.emailErrors where e.state = ?1")
    List<ProcessEmail> findByState(EmailState emailState);

}
