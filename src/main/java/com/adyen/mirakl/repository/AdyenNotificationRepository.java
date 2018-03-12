package com.adyen.mirakl.repository;

import com.adyen.mirakl.domain.AdyenNotification;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the AdyenNotification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AdyenNotificationRepository extends JpaRepository<AdyenNotification, Long> {

    AdyenNotification findOneById(Long id);

}
