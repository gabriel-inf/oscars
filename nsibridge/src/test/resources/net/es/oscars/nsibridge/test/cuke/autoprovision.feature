Feature: set up oscars circuit when start time comes, tear down after end time

    Scenario: set up when start time arrives
        Given I have set up the run environment
        Given I wait up to 30 sec until any previous tasks complete

        When I generate a reservation request
        When I set "start" time to 0 sec
        When I set "end" time to 120 sec

        When I assign random connId and corrId

        When I submit reserve
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"

        When I assign a random corrId
        When I submit reserveCommit
        Then I wait up to 10000 ms until the "RSM" state is: "ReserveStart"


        When I assign a random corrId
        When I submit provision

        Then I wait up to 5000 ms until the "PSM" state is: "Provisioned"
        When I wait up to 5000 ms until provMonitor schedules "SETUP"

        Given I wait up to 30 sec until any previous tasks complete
        Then the dataplaneStatus is "active"
        Given I clear all existing tasks


