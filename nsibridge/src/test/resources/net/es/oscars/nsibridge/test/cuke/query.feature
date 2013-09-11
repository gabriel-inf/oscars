Feature: query reservation

    Scenario: query a new reservation
        Given I have set up the run environment
        Given I wait up to 30 sec until any previous tasks complete

        When I assign random connId and corrId

        When I set the current NSA requester to "foo"
        When I generate a reservation request
        When I set the version to 0

        When I submit reserve
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveChecking"


        When I assign a random corrId
        Then querySummarySync() returns resvState "ReserveChecking"
        Given I clear all existing tasks
