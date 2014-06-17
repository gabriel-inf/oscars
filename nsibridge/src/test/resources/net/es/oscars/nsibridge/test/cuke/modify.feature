Feature: modifying existing reservations

    I want to verify I can modify a new reservation

    Scenario: reserve, commit, then modify
        Given I have set up the run environment
        Given I wait up to 30 sec until any previous tasks complete

        Given I clear all existing tasks

        When I generate a reservation request
        When I set the version to 0
        When I set "start" time to 0 sec

        When I assign random connId and corrId

        When I submit reserve
        Then the last submit has not thrown an exception
        When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"
        Then I can find 1 resvRecord entries
        Then the resvRecord with version 0 "has not" been committed

        When I assign a random corrId
        When I submit reserveCommit
        Then I know the simpleRequest taskIds

        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then the "RSM" state is: "ReserveStart"
        Then the resvRecord with version 0 "has" been committed

        Then I can get the dataplane record with version 0
        Then the dataplane record "is not" active




        When I assign a random corrId
        When I submit provision

        Then I wait up to 5000 ms until the "PSM" state is: "Provisioned"
        When I wait up to 5000 ms until provMonitor schedules "SETUP"
        Given I wait up to 30 sec until any previous tasks complete


        Then I can get the dataplane record with version 0
        Then the dataplane record "is" active




        When I assign a random corrId
        When I set the version to 1
        When I set the capacity to 200
        When I submit reserve
        Then the last submit has not thrown an exception
        Then the "RSM" state is: "ReserveChecking"
        When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"
        Then I can find 2 resvRecord entries

        When I assign a random corrId
        When I submit reserveCommit
        When I wait up to 10000 ms until the "RSM" state is: "ReserveStart"

        Then I can find 2 resvRecord entries
        Then the resvRecord with version 0 "has" been committed
        Then the resvRecord with version 1 "has" been committed

        Then I can get the dataplane record with version 0
        Then the dataplane record "is" active

        Then I can get the dataplane record with version 1
        Then the dataplane record "is" active
        Given I wait up to 30 sec until any previous tasks complete
