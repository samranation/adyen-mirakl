package com.adyen.mirakl.service;

import com.adyen.Util.Util;
import com.adyen.mirakl.domain.AdyenPayoutError;
import com.adyen.mirakl.repository.AdyenPayoutErrorRepository;
import com.adyen.model.Amount;
import com.adyen.model.marketpay.*;
import com.adyen.service.Account;
import com.adyen.service.Fund;
import com.adyen.service.exception.ApiException;
import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
public class PayoutService {

    private final Logger log = LoggerFactory.getLogger(PayoutService.class);

    @Resource
    private Account adyenAccountService;

    @Resource
    private Fund adyenFundService;

    @Resource
    private AdyenPayoutErrorRepository adyenPayoutErrorRepository;

    protected final static Gson GSON = new Gson();


    public void parseMiraklCsv(String csvData) throws IOException {
        Iterable<CSVRecord> records = null;
        records = CSVParser.parse(csvData, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';'));
        for (CSVRecord record : records) {
            String accountHolderCode = record.get("shop-id");
            String amount = record.get("transfer-amount");
            String currency = record.get("currency-iso-code");
            String iban = record.get("payment-info-ibantype-iban");
            String invoiceNumber = record.get("invoice-number");
            String shopName = record.get("shop-name");
            String description = "Payout shop " + shopName + " (" + accountHolderCode + "), " + "Invoice number: " + invoiceNumber;

            PayoutAccountHolderRequest payoutAccountHolderRequest = null;
            PayoutAccountHolderResponse payoutAccountHolderResponse = null;

            try {
                payoutAccountHolderRequest = createPayoutAccountHolderRequest(accountHolderCode, amount, currency, iban, description);
                payoutAccountHolderResponse = adyenFundService.payoutAccountHolder(payoutAccountHolderRequest);
                log.info("Payout submitted for accountHolder: [{}] + Psp ref: [{}]", accountHolderCode, payoutAccountHolderResponse.getPspReference());
            } catch (ApiException e) {
                log.error("MP exception: " + e.getError(), e);
                storeAdyenPayoutError(payoutAccountHolderRequest, payoutAccountHolderResponse);
            } catch (Exception e) {
                log.error("MP exception: " + e.getMessage(), e);
            }
        }

    }

    /**
     * Store Payout request into database so we can do retries
     */
    protected void storeAdyenPayoutError(PayoutAccountHolderRequest payoutAccountHolderRequest, PayoutAccountHolderResponse payoutAccountHolderResponse) {
        if (payoutAccountHolderRequest != null) {
            String rawRequest = GSON.toJson(payoutAccountHolderRequest);
            AdyenPayoutError adyenPayoutError = new AdyenPayoutError();

//            adyenPayoutError.setAccountHolderCode(payoutAccountHolderRequest.getAccountHolderCode());
            adyenPayoutError.setRawRequest(rawRequest);

            if (payoutAccountHolderResponse != null) {
                String rawResponse = GSON.toJson(payoutAccountHolderResponse);
                adyenPayoutError.setRawResponse(rawResponse);
            }

            adyenPayoutError.setProcessing(false);
            adyenPayoutError.setRetry(0);
            adyenPayoutError.setCreatedAt(ZonedDateTime.now());
            adyenPayoutError.setUpdatedAt(ZonedDateTime.now());
            adyenPayoutErrorRepository.save(adyenPayoutError);
        }
    }

    protected PayoutAccountHolderResponse payoutAccountHolder(PayoutAccountHolderRequest payoutAccountHolderRequest) throws Exception {
        return adyenFundService.payoutAccountHolder(payoutAccountHolderRequest);
    }

    protected PayoutAccountHolderRequest createPayoutAccountHolderRequest(String accountHolderCode, String amount, String currency, String iban, String description) throws Exception {

        //Call Adyen to retrieve the accountCode from the accountHolderCode
        GetAccountHolderResponse accountHolderResponse = getAccountHolderResponse(accountHolderCode);
        String accountCode = getAccountCode(accountHolderResponse);

        //Retrieve the bankAccountUUID from Adyen matching to the iban provided from Mirakl
        String bankAccountUUID = getBankAccountUUID(accountHolderResponse, iban);
        PayoutAccountHolderRequest payoutAccountHolderRequest = new PayoutAccountHolderRequest();
        payoutAccountHolderRequest.setAccountCode(accountCode);
        payoutAccountHolderRequest.setBankAccountUUID(bankAccountUUID);
        payoutAccountHolderRequest.setAccountHolderCode(accountHolderCode);
        payoutAccountHolderRequest.setDescription(description);
        Amount adyenAmount = Util.createAmount(amount, currency);
        payoutAccountHolderRequest.setAmount(adyenAmount);

        return payoutAccountHolderRequest;
    }

    protected GetAccountHolderResponse getAccountHolderResponse(String accountHolderCode) throws Exception {
        GetAccountHolderRequest getAccountHolderRequest = new GetAccountHolderRequest();
        getAccountHolderRequest.setAccountHolderCode(accountHolderCode);
        return adyenAccountService.getAccountHolder(getAccountHolderRequest);
    }

    private String getAccountCode(GetAccountHolderResponse accountHolderResponse) {
        return accountHolderResponse.getAccounts().get(0).getAccountCode();
    }

    protected String getBankAccountUUID(GetAccountHolderResponse accountHolderResponse, String iban) {
        //Iban Check
        List<BankAccountDetail> bankAccountDetailList = accountHolderResponse.getAccountHolderDetails().getBankAccountDetails();
        if (! bankAccountDetailList.isEmpty()) {
            for (BankAccountDetail bankAccountDetail : bankAccountDetailList) {
                if (bankAccountDetail.getIban().equals(iban)) {
                    return bankAccountDetail.getBankAccountUUID();
                }
            }
        }
        throw new IllegalStateException("No matching Iban between Mirakl and Adyen platforms.");
    }


}
