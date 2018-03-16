package com.adyen.mirakl.service;

import com.adyen.mirakl.domain.ShareholderMapping;
import com.adyen.mirakl.repository.ShareholderMappingRepository;
import com.adyen.model.marketpay.AccountHolderDetails;
import com.adyen.model.marketpay.CreateAccountHolderResponse;
import com.adyen.model.marketpay.ShareholderContact;
import com.adyen.model.marketpay.UpdateAccountHolderResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ShareholderMappingService {

    @Resource
    private ShareholderMappingRepository shareholderMappingRepository;

    public void updateShareholderMapping(CreateAccountHolderResponse response) {
        AccountHolderDetails accountHolderDetails = response.getAccountHolderDetails();
        if (shareholdersDoNotExist(accountHolderDetails)) {
            return;
        }
        String shopCode = response.getAccountHolderCode();
        updateMapping(shopCode, accountHolderDetails);
    }

    public void updateShareholderMapping(UpdateAccountHolderResponse response) {
        AccountHolderDetails accountHolderDetails = response.getAccountHolderDetails();
        if (shareholdersDoNotExist(accountHolderDetails)) {
            return;
        }
        String shopCode = response.getAccountHolderCode();
        updateMapping(shopCode, accountHolderDetails);
    }

    private void updateMapping(String shopCode, AccountHolderDetails accountHolderDetails) {
        List<String> shareholderCodes = accountHolderDetails.getBusinessDetails().getShareholders().stream()
            .map(ShareholderContact::getShareholderCode).collect(Collectors.toList());
        IntStream.range(0, shareholderCodes.size())
            .forEach(i -> {
                ShareholderMapping shareholderMapping = shareholderMappingRepository.findOneByMiraklShopIdAndMiraklUboNumber(shopCode, i + 1).orElseGet(ShareholderMapping::new);
                shareholderMapping.setMiraklUboNumber(i + 1);
                shareholderMapping.setAdyenShareholderCode(shareholderCodes.get(i));
                shareholderMapping.setMiraklShopId(shopCode);
                shareholderMappingRepository.save(shareholderMapping);
            });
        shareholderMappingRepository.flush();
    }

    private boolean shareholdersDoNotExist(AccountHolderDetails accountHolderDetails) {
        return accountHolderDetails == null || accountHolderDetails.getBusinessDetails() == null || CollectionUtils.isEmpty(accountHolderDetails.getBusinessDetails().getShareholders());
    }

}
