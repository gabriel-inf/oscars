Feature: query reservation

    Scenario: query a new reservation
        Given I have set up the run environment

        When I assign random connId and corrId

        When I submit reserve
        When I assign a random corrId
        Then querySummarySync() returns resvState "ReserveChecking"
