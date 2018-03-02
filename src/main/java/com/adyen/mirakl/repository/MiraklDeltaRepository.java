package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.MiraklDelta;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the MiraklDelta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MiraklDeltaRepository extends JpaRepository<MiraklDelta, Long> {

    Optional<MiraklDelta> findFirstByOrderByIdDesc();

}
