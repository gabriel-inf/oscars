Feature: creating new reservations

    I want to verify I can create a new reservation

    Scenario: Complete reservation lifecycle
        Given I have set up the run environment
        Given I clear all existing tasks
        Then I log "======= Complete - 1"

        When I generate a reservation request
        When I set the version to 0

        Given that I know the count of all pending reservation requests

        When I assign random connId and corrId

        When I submit reserve
        Then the last submit has not thrown an exception
        Then the count of ConnectionRecords is 1
        Then the count of pending reservation requests has changed by 1
        Then the "RSM" state is: "ReserveChecking"
        Then the ResvRequest has OscarsOp: "RESERVE"

        Then I know the reserve taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then I know the OSCARS gri
        Then the "RSM" state is: "ReserveHeld"


        When I assign a random corrId
        When I submit reserveCommit
        Then the last submit has not thrown an exception
        Then I know the simpleRequest taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then the "RSM" state is: "ReserveStart"

