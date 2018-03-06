Feature: Payout notifications for seller payout

    @ADY-9
    Scenario: Successful payout notification is received upon successful seller payout
        Given a AccountHolder exists who has passed KYC checks and is eligible for payout
            | seller       |
            | PayoutShop01 |
        When a payment voucher is sent to the App from Mirakl
        Then adyen will send the ACCOUNT_HOLDER_PAYOUT notification
            | currency | amount | statusCode | iban                   |
            | EUR      | 29.14  | Initiated  | GB26TEST40051512347366 |

    @ADY-9
    Scenario: Failure status is received for payout notification
        Given a AccountHolder exists who has passed KYC checks and is eligible for payout
            | seller       |
            | PayoutShop02 |
        When a payment voucher is sent to the App from Mirakl
        Then adyen will send the ACCOUNT_HOLDER_PAYOUT notification with statusCode
            | statusCode |
            | Failed     |
