Feature: Bank Account Verification

    @ADY-13
    Scenario Outline: ACCOUNT_HOLDER_VERIFICATION notification is received after seller provides Bank Account Details
        Given a shop has been created in Mirakl with a corresponding account holder in Adyen with the following data
            | iban   | name   | bic   | bank name | city   |
            | <iban> | <name> | <bic> | RBS       | <city> |
        When we process the data and push to Adyen
        Then the ACCOUNT_HOLDER_VERIFICATION notification is sent by Adyen comprising of BANK_ACCOUNT_VERIFICATION and <verificationStatus>
        Examples:
            | verificationStatus | iban                   | name         | bic         | city        |
            | PASSED             | DE87123456781234567890 | A. Schneider | GENODEFF123 |             |
            | AWAITING_DATA      | DE87123456781234567890 | A. Schneider | XXXXXXXXXXX |             |
            | INVALID_DATA       | DE87123456781234567890 | A. Schneider | GENODEFF123 | INVALIDDATA |
            | PENDING            |                        |              |             |             |
