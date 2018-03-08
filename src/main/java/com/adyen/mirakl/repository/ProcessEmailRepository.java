package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.ProcessEmail;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the ProcessEmail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProcessEmailRepository extends JpaRepository<ProcessEmail, Long> {

    @Query("select e from ProcessEmail e where e.to = ?1 and e.subject = ?2 and e.content = ?3 and e.multipart = ?4 and e.html = ?5")
    Optional<ProcessEmail> findExisting(String to, String subject, String content, boolean isMultipart, boolean isHtml);

}
