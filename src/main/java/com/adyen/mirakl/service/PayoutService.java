package com.adyen.mirakl.service;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.adyen.Util.Util;
import com.adyen.model.Amount;
import com.adyen.model.marketpay.BankAccountDetail;
import com.adyen.model.marketpay.GetAccountHolderRequest;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.model.marketpay.PayoutAccountHolderRequest;
import com.adyen.model.marketpay.PayoutAccountHolderResponse;
import com.adyen.service.Account;
import com.adyen.service.Fund;
import com.adyen.service.exception.ApiException;


@Service
@Transactional
public class PayoutService {

    private final Logger log = LoggerFactory.getLogger(PayoutService.class);

    @Resource
    private Account adyenAccountService;

    @Resource
    private Fund adyenFundService;


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

            try {
                PayoutAccountHolderRequest payoutAccountHolderRequest = payoutAccountHolder(accountHolderCode, amount, currency, iban, description);
                PayoutAccountHolderResponse payoutAccountHolderResponse = adyenFundService.payoutAccountHolder(payoutAccountHolderRequest);
                System.out.println(payoutAccountHolderResponse);
            } catch (ApiException e) {
                log.warn("MP exception: " + e.getError());
            } catch (Exception e) {
                log.warn("MP exception: " + e.getMessage());
            }
        }

    }

    protected PayoutAccountHolderRequest payoutAccountHolder(String accountHolderCode, String amount, String currency, String iban, String description) throws Exception {

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
        GetAccountHolderResponse getAccountHolderResponse = adyenAccountService.getAccountHolder(getAccountHolderRequest);
        return getAccountHolderResponse;
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
        throw new RuntimeException("No matching Iban between Mirakl and Adyen platforms.");
    }


}
