package com.adyen.mirakl.cucumber.stepdefs.helpers.hooks;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.adyen.model.Amount;
import com.adyen.model.marketpay.AccountHolderBalanceRequest;
import com.adyen.model.marketpay.AccountHolderBalanceResponse;
import com.adyen.model.marketpay.TransferFundsResponse;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;

import java.util.List;

public class CucumberHooks extends StepDefsHelper {

    @Before
    public void setDefaultAwaitilityTimeOut() {
        Awaitility.setDefaultTimeout(Duration.FIVE_MINUTES);
    }

    @Before
    public void clearCucumberMap() {
        cucumberMap.clear();
    }

    @Before
    public void ensureBalanceIsInsufficient(Scenario scenario) throws   Exception {
        if (scenario.getSourceTagNames().contains("@ADY-34")) {
            String accountHolderCode = shopConfiguration.getShopIds().get("PayoutShop04").toString();
            Integer sourceAccountCode = adyenAccountConfiguration.getAccountCode().get("PayoutShop04");
            Integer destinationAccountCode = adyenAccountConfiguration.getAccountCode().get("alessioIndividualTestPayout2");

            AccountHolderBalanceRequest accountHolderBalanceRequest = new AccountHolderBalanceRequest();
            accountHolderBalanceRequest.setAccountHolderCode(accountHolderCode);
            AccountHolderBalanceResponse accountHolderBalanceResponse = adyenFundService.AccountHolderBalance(accountHolderBalanceRequest);
            List<Amount> balance = accountHolderBalanceResponse.getTotalBalance().getBalance();
            Long balanceValue = balance.stream().map(Amount::getValue).findAny().orElse(null);
            Assertions.assertThat(balanceValue).isNotNull();

            Long insufficientBalance = 100L;
            if (!balanceValue.equals(insufficientBalance) && balanceValue > insufficientBalance) {
                Long transferAmount = balanceValue - insufficientBalance;
                TransferFundsResponse response = transferFundsAndRetrieveResponse(transferAmount, sourceAccountCode, destinationAccountCode);
                Assertions.assertThat(response.getResultCode()).isEqualTo("Received");
            } else if (balanceValue.equals(0L)){
                TransferFundsResponse response = transferFundsAndRetrieveResponse(insufficientBalance, destinationAccountCode, sourceAccountCode);
                Assertions.assertThat(response.getResultCode()).isEqualTo("Received");
            }
        }
    }
}
