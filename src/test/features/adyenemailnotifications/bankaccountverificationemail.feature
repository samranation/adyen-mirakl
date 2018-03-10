@cucumber
Feature: Bank Account Verification email notification

    @ADY-23 @exclude
    Scenario: KYCCheckSummary - Bank Statement message is sent to seller and operator (BCC)
        Given a shop has been created in Mirakl for an Individual with Bank Information
            | city        | bank name | iban                   | bankOwnerName | lastName |
            | INVALIDDATA | testBank  | GB26TEST40051512347366 | TestData      | TestData |
        And we process the data and push to Adyen
        When a RETRY_LIMIT_REACHED verificationStatus has been sent to the Connector
            """
            {
              "eventDate": "2018-03-09T08:21:10+01:00",
              "eventType": "ACCOUNT_HOLDER_VERIFICATION",
              "executingUserKey": "Account Holder Update",
              "live": "false",
              "pspReference": "x",
              "content": {
                "accountHolderCode": "x",
                "bankAccountUUID": "x",
                "statusSummary": {
                  "code": 1606,
                  "description": "The maximum amount of retries has been surpassed, please provide a document."
                },
                "statusSummaryItems": [{
                  "KYCCheckDataSummaryItem": {
                    "itemCode": 1606,
                    "itemDescription": "The maximum amount of retries has been surpassed, please provide a document."
                  }
                }],
                "verificationStatus": "RETRY_LIMIT_REACHED",
                "verificationType": "BANK_ACCOUNT_VERIFICATION"
              }
            }
            """
        Then an email will be sent to the seller

