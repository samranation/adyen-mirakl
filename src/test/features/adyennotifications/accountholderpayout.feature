@cucumber
Feature: Payout notifications for seller payout

    @ADY-9 @ADY-86
    Scenario: Successful payout notification is received upon successful seller payout
        Given a AccountHolder exists who is eligible for payout
            | seller       | allowPayout |
            | PayoutShop01 | true        |
        When a payment voucher is sent to the Connector
            | paymentVoucher                  |
            | PaymentVoucher_PayoutShop01.csv |
        Then adyen will send the ACCOUNT_HOLDER_PAYOUT notification
            | currency | amount | statusCode | iban                   |
            | EUR      | 2914.0 | Initiated  | GB26TEST40051512347366 |

    @ADY-9 @ADY-86
    Scenario: Failure status is received for payout notification
        Given a AccountHolder exists who is not eligible for payout
            | seller       | allowPayout |
            | PayoutShop02 | false       |
        When a payment voucher is sent to the Connector
            | paymentVoucher                  |
            | PaymentVoucher_PayoutShop02.csv |
        Then adyen will send the ACCOUNT_HOLDER_PAYOUT notification with status
            | statusCode | message                                           |
            | Failed     | There is not enough balance available for account |

    @ADY-34 @bug @ADY-111
    Scenario: The connector forces payout-retry upon accountHolder payable state change
        Given a AccountHolder exists who is eligible for payout with insufficient funds
            | seller       | allowPayout | balance |
            | PayoutShop04 | true        | 100     |
        When a payment voucher is sent to the Connector
            | paymentVoucher                  |
            | PaymentVoucher_PayoutShop04.csv |
        Then adyen will send the ACCOUNT_HOLDER_PAYOUT notification with status
            | statusCode | message                                           |
            | Failed     | There is not enough balance available for account |
        When the accountHolders balance is increased
            | source accountHolderCode     | transfer amount | destination accountHolderCode |
            | alessioIndividualTestPayout2 | 9900            | PayoutShop04                  |
        And a notification will be sent in relation to the balance change
            | eventType                    | reason                   |
            | ACCOUNT_HOLDER_STATUS_CHANGE | transfer has been booked |
        Then the Connector will trigger payout retry
        And adyen will send the ACCOUNT_HOLDER_PAYOUT notification
            | currency | amount | statusCode | iban                   |
            | EUR      | 9900.0 | Initiated  | GB26TEST40051512347366 |
        And the failed payout record is removed from the Connector database
