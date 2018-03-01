Feature: Bank Account Verification

    @ADY-13
    Scenario Outline: ACCOUNT_HOLDER_VERIFICATION notification is received after seller provides Bank Account Details
        Given a shop has been created in Mirakl with a corresponding account holder in Adyen with the following data
            | name   | bank name | legalEntity |
            | <name> | RBS       | Individual  |
        When we process the data and push to Adyen
        Then the ACCOUNT_HOLDER_VERIFICATION notification is sent by Adyen comprising of BANK_ACCOUNT_VERIFICATION and <verificationStatus>
        Examples:
            | verificationStatus | name         |
            | AWAITING_DATA      | A. Schneider |
