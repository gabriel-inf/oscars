Feature: terminate side effects


    Scenario: Reserve, commit, terminate. Try provision, reserve, terminate (should fail)
        Given I have set up the run environment

        Given that I know the count of all pending reservation requests
        When I assign random connId and corrId

        When I submit reserve
        Then the "RSM" state is: "ReserveChecking"

        Then I know the reserve taskIds
        When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"
        Then I know the OSCARS gri

        When I assign a random corrId
        When I submit reserveCommit
        When I wait up to 10000 ms until the "RSM" state is: "ReserveStart"

        When I assign a random corrId
        When I submit terminate
        When I wait up to 10000 ms until the "LSM" state is: "Terminated"

        When I assign a random corrId
        When I submit reserve
        Then the last submit has thrown an exception

        When I assign a random corrId
        When I submit provision
        Then the last submit has thrown an exception

        When I assign a random corrId
        When I submit terminate
        Then the last submit has thrown an exception
