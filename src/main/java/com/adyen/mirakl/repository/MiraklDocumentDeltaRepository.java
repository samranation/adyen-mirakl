package com.adyen.mirakl.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.adyen.mirakl.domain.MiraklDocumentDelta;


/**
 * Spring Data JPA repository for the MiraklDocumentDelta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MiraklDocumentDeltaRepository extends JpaRepository<MiraklDocumentDelta, Long> {

    Optional<MiraklDocumentDelta> findFirstByOrderByIdDesc();
}
