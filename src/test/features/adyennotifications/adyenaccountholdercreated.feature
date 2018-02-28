Feature: Seller Account Management

    @ADY-6
    Scenario: Creating Mirakl shop will create Adyen Account Holder for an Individual
        Given a new shop has been created in Mirakl
            | legalEntity |
            | Individual  |
        When we process the data and push to Adyen
        Then an AccountHolder will be created in Adyen with status Active
        And a notification will be sent pertaining to ACCOUNT_HOLDER_CREATED
        And the shop data is correctly mapped to the Adyen Account

    @ADY-53
    Scenario: Adding more shareholders will update the existing accountHolder
        Given a complete shareholder detail is submitted on Mirakl
        When a new shop has been created in Mirakl
            | legalEntity | maxUbos |
            | Business    | 4       |
        And we process the data and push to Adyen
        Then a notification will be sent pertaining to ACCOUNT_HOLDER_CREATED
        And the account holder is created in Adyen with status Active
        And the shop data is correctly mapped to the Adyen Business Account

    @ADY-53
    Scenario: Don't create Adyen Account Holder if no shareholder data has been entered
        Given a new shop has been created in Mirakl
            | legalEntity |
            | Business    |
        When a complete shareholder is not provided
        Then no account holder is created in Adyen
