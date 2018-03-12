@cucumber
Feature: Bank Account Verification email notification

    @ADY-23 @exclude
    Scenario: KYCCheckSummary - Bank Statement message is sent to seller and operator (BCC)
        Given a shop has been created in Mirakl for an Individual with Bank Information
            | city        | bank name | iban                   | bankOwnerName | lastName |
            | INVALIDDATA | testBank  | GB26TEST40051512347366 | TestData      | TestData |
        And we process the data and push to Adyen
        When Invalid data is provided to Mirakl and pushed to Adyen multiple times
            | firstName | lastName | postCode | city        |
            | Test1     | Data1    | SE1 9BG  | INVALIDDATA |
            | Test2     | Data2    | SE2 9BG  | INVALIDDATA |
            | Test3     | Data3    | SE3 9BG  | INVALIDDATA |
            | Test4     | Data4    | SE4 9BG  | INVALIDDATA |
            | Test5     | Data5    | SE5 9BG  | INVALIDDATA |
        Then the ACCOUNT_HOLDER_STATUS_CHANGE notification is sent by Adyen comprising of BANK_ACCOUNT_VERIFICATION and RETRY_LIMIT_REACHED

