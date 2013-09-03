@wip
Feature: timeout reservation

    I want to verify I the reservation times out

    Scenario: Exercise timeout reservation - commit
        Given I have set up the run environment

        When I assign random connId and corrId
        When I set the reserveTimeout to 1000 ms

        When I generate a reservation request

        When I submit reserve
        Then the last submit has not thrown an exception
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"


        Then I wait up to 10000 ms until the "RSM" state is: "ReserveTimeout"
        Then I wait 5000 milliseconds

        Then the "RSM" state is: "ReserveTimeout"

        When I assign a random corrId
        When I submit reserveCommit
        Then the last submit has not thrown an exception
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveStart"

        Then I restore the reserveTimeout value


    Scenario: Exercise timeout reservation - abort
        Given I have set up the run environment

        When I assign random connId and corrId
        When I set the reserveTimeout to 1000 ms

        When I generate a reservation request

        When I submit reserve
        Then the last submit has not thrown an exception
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveTimeout"

        When I assign a random corrId
        When I submit reserveAbort
        Then the last submit has not thrown an exception

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveStart"
        Then I restore the reserveTimeout value