Feature: timeout reservation

    I want to verify I the reservation times out

    Scenario: Exercise timeout reservation - commit
        Given I have set up the run environment
        Given I clear all existing tasks

        When I assign random connId and corrId
        When I set the reserveTimeout to 1 seconds

        When I generate a reservation request

        When I submit reserve
        Then the last submit has not thrown an exception

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveTimeout"
        Then I wait 5000 milliseconds

        Then the "RSM" state is: "ReserveTimeout"

        When I assign a random corrId
        When I submit reserveCommit
        Then the last submit has not thrown an exception
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveStart"

        Then I restore the reserveTimeout value
        Given I clear all existing tasks


    Scenario: Exercise timeout reservation - abort
        Given I have set up the run environment
        Given I clear all existing tasks

        When I assign random connId and corrId
        When I set the reserveTimeout to 1 seconds

        When I generate a reservation request

        When I submit reserve
        Then the last submit has not thrown an exception

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveTimeout"

        When I assign a random corrId
        When I submit reserveAbort
        Then the last submit has not thrown an exception

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveStart"
        Then I restore the reserveTimeout value
        Given I clear all existing tasks

    Scenario: reserve, commit, modify, timeout, abort, rollback
        Given I have set up the run environment
        Given I clear all existing tasks
        When I set the reserveTimeout to 1 seconds

        When I generate a reservation request
        When I set the version to 0

        When I assign random connId and corrId

        When I submit reserve
        Then the last submit has not thrown an exception
        When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"
        Then I can find 1 resvRecord entries
        Then the resvRecord with version 0 "has not" been committed

        When I assign a random corrId
        When I submit reserveCommit
        When I wait up to 10000 ms until the "RSM" state is: "ReserveStart"
        Then the resvRecord with version 0 "has" been committed


        When I assign a random corrId
        When I set the version to 1
        When I set the capacity to 200
        When I submit reserve
        Then the last submit has not thrown an exception
        Then the "RSM" state is: "ReserveChecking"
        When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"
        Then I can find 2 resvRecord entries
        Then the resvRecord with version 0 "has" been committed
        Then the resvRecord with version 1 "has not" been committed

        When I wait up to 60000 ms until the "RSM" state is: "ReserveTimeout"

        When I assign a random corrId
        When I submit reserveAbort
        Then I wait up to 5000 ms until the "RSM" state is: "ReserveStart"
        Then I can find 1 resvRecord entries

        Then I restore the reserveTimeout value
        Given I clear all existing tasks
