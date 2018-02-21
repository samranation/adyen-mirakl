Feature: Seller Account Management

    @ADY-6
    Scenario: Creating Mirakl shop will create Adyen Account Holder
        Given a new shop has been created in Mirakl
        When we process the data and push to Adyen
        Then an AccountHolder will be created in Adyen with status Active
        And a notification will be sent pertaining to ACCOUNT_HOLDER_CREATED
