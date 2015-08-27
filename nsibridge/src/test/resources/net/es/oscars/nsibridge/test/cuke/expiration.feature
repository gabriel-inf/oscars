Feature: reservation expiration
    I want to verify that reservations expire after end time is reached

    Scenario: Exercise reservation expiration
        Given I have set up the run environment
        Given I clear all existing tasks

        When I assign random connId and corrId
        When I generate a reservation request
        When I set "start" time to 5 sec
        When I set "end" time to 15 sec


        Given that I know the count of all pending reservation requests

        When I assign random connId and corrId

        When I submit reserve
        Then the last submit has not thrown an exception
        When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"

        When I assign a random corrId
        When I submit reserveCommit
        When I wait up to 10000 ms until the "RSM" state is: "ReserveStart"

        When I assign a random corrId
        When I submit provision
        Then the last submit has not thrown an exception
        When I wait up to 10000 ms until the "PSM" state is: "Provisioned"


        When I wait 25000 milliseconds
        Then the "LSM" state is: "PassedEndTime"
        Then the "PSM" state is: "Released"
        Then I can get the dataplane record with version 0
        Then the dataplane record "is not" active
        Then I have sent a dataplane update with version 0 and "is not" active