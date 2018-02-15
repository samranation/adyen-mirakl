Feature: Configuration Notification

    @ADY-37
    Scenario Outline: create and delete configuration notification
        Given a configuration for the notification <eventType> has been created
#        When a call has been made to the /deleteNotificationConfigurations endpoint
#        And notificationIds have been passed as parameters
#        Then configuration notification will be successfully deleted
#        And a pspReference will be returned as a response
        Examples:
            | eventType                    |
            | ACCOUNT_HOLDER_CREATED       |
            | ACCOUNT_CREATED              |
            | ACCOUNT_HOLDER_UPDATED       |
            | ACCOUNT_HOLDER_STATUS_CHANGE |
            | ACCOUNT_HOLDER_VERIFICATION  |
            | ACCOUNT_HOLDER_LIMIT_REACHED |
            | ACCOUNT_HOLDER_PAYOUT        |
            | PAYMENT_FAILURE              |
            | SCHEDULED_REFUNDS            |
            | TRANSFER_FUNDS               |
            | REPORT_AVAILABLE             |
            | COMPENSATE_NEGATIVE_BALANCE  |
