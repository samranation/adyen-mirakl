Feature: Account Holder Updated notification upon Mirakl shop changes

    @ADY-11 @ADY-71 @bug
    Scenario: Updating Mirakl existing shop and verifying Adyen Account Holder Details are updates
        Given a new shop has been created in Mirakl
            | legalEntity | lastName |
            | Individual  | TestData |
        And we process the data and push to Adyen
        And an AccountHolder will be created in Adyen with status Active
        When the Mirakl Shop Details have been updated
            | firstName | lastName | postCode | city       |
            | John      | Smith    | SE1 9GB  | Manchester |
        And we process the data and push to Adyen
        Then a notification will be sent pertaining to ACCOUNT_HOLDER_UPDATED
        And the shop data is correctly mapped to the Adyen Account

    @ADY-11
    Scenario: ACCOUNT_HOLDER_UPDATED will not be invoked if no data has been changed
        Given a new shop has been created in Mirakl
            | legalEntity | lastName |
            | Individual  | TestData |
        And we process the data and push to Adyen
        And an AccountHolder will be created in Adyen with status Active
        When we process the data and push to Adyen
        Then a notification of ACCOUNT_HOLDER_UPDATED will not be sent
