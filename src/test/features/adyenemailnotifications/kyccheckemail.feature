@cucumber
Feature: KYC check intervention emails

    @ADY-17
    Scenario: email is sent to seller upon IDENTITY_VERIFICATION AWAITING_DATA notification
        Given a new Business shop has been created in Mirakl with some Mandatory data missing
            | maxUbos | lastName |
            | 4       | testData |
        And we process the data and push to Adyen
        Then adyen will send multiple ACCOUNT_HOLDER_VERIFICATION notifications with IDENTITY_VERIFICATION of status AWAITING_DATA
        When the notifications are sent to Connector App
        Then a remedial email will be sent for each ubo
        """
        Account verification, awaiting data
        """

    @ADY-17 @ADY-112
    Scenario: email is sent to seller upon IDENTITY_VERIFICATION INVALID_DATA notification
        Given a new Business shop has been created in Mirakl with invalid data
            | maxUbos | lastName    | city        |
            | 4       | INVALIDDATA | INVALIDDATA |
        And we process the data and push to Adyen
        When the IDENTITY_VERIFICATION notifications containing INVALID_DATA status are sent to the Connector for each UBO
        Then a remedial email will be sent for each ubo
        """
        Account verification, invalid data
        """
