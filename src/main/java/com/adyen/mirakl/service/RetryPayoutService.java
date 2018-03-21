package com.adyen.mirakl.service;

import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.adyen.mirakl.config.ApplicationProperties;
import com.adyen.mirakl.domain.AdyenPayoutError;
import com.adyen.mirakl.repository.AdyenPayoutErrorRepository;
import com.adyen.model.marketpay.PayoutAccountHolderRequest;
import com.adyen.model.marketpay.PayoutAccountHolderResponse;
import com.adyen.service.Fund;
import com.adyen.service.exception.ApiException;
import com.google.common.reflect.TypeToken;
import static com.adyen.mirakl.service.PayoutService.GSON;


@Service
@Transactional
public class RetryPayoutService {

    private final Logger log = LoggerFactory.getLogger(RetryPayoutService.class);

    @Resource
    private AdyenPayoutErrorRepository adyenPayoutErrorRepository;

    @Resource
    private ApplicationProperties applicationProperties;

    @Resource
    private Fund adyenFundService;

    public void retryFailedPayouts() {
        final List<AdyenPayoutError> failedPayouts = adyenPayoutErrorRepository.findByRetry(applicationProperties.getMaxPayoutFailed());
        if (CollectionUtils.isEmpty(failedPayouts)) {
            log.info("No failed payouts found");
            return;
        }

        failedPayouts.forEach(adyenPayoutError -> {

            PayoutAccountHolderResponse payoutAccountHolderResponse = null;
            try {
                payoutAccountHolderResponse = adyenFundService.payoutAccountHolder(GSON.fromJson(adyenPayoutError.getRawRequest(), new TypeToken<PayoutAccountHolderRequest>() {
                }.getType()));
            } catch (ApiException e) {
                log.error("Failed retry payout exception: " + e.getError(), e);
                adyenPayoutError.setRetry(adyenPayoutError.getRetry() + 1);

                if (payoutAccountHolderResponse != null) {
                    String rawResponse = GSON.toJson(payoutAccountHolderResponse);
                    adyenPayoutError.setRawResponse(rawResponse);
                }
                adyenPayoutErrorRepository.save(adyenPayoutError);
            } catch (Exception e) {
                log.error("Failed retry payout exception: " + e.getMessage(), e);
            }
        });
    }
}
