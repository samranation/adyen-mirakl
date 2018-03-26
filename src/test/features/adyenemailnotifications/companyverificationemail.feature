@cucumber
Feature: Company verification email

    @ADY-87
    Scenario: Remedial email is sent to seller upon COMPANY_VERIFICATION notification of status AWAITING_DATA
        Given a new Business shop has been created in Mirakl with some Mandatory data missing
            | maxUbos | lastName |
            | 4       | testData |
        And we process the data and push to Adyen
        When the COMPANY_VERIFICATION notifications containing AWAITING_DATA status are sent to the Connector
        Then an email will be sent to the seller
        """
        Awaiting company ID data
        """

    @ADY-87 @ADY-114
    Scenario: Remedial email is sent to seller upon COMPANY_VERIFICATION notification of status INVALID_DATA
        Given a new Business shop has been created in Mirakl with some Mandatory data missing
            | maxUbos | lastName |
            | 4       | testData |
        And we process the data and push to Adyen
        When the COMPANY_VERIFICATION notifications containing INVALID_DATA status are sent to the Connector
        Then an email will be sent to the seller
        """
        Awaiting company ID data
        """
