@cucumber
Feature: Smoke test

    Scenario: Ping test to ensure endpoint is receiving notifications
        Given a shop exists in Mirakl
            | seller       |
            | UpdateShop01 |
        When the Mirakl Shop Details have been changed
        And we process the data and push to Adyen
        Then a notification will be sent pertaining to ACCOUNT_HOLDER_UPDATED

