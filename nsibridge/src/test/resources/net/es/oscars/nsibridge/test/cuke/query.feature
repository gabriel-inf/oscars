Feature: query reservation

    Scenario: query a new reservation
        Given I have set up the run environment
        Given I wait up to 30 sec until any previous tasks complete

        When I assign random connId and corrId

        When I submit reserve
        When I assign a random corrId
        Then querySummarySync() returns resvState "ReserveChecking"
        Given I clear all existing tasks
