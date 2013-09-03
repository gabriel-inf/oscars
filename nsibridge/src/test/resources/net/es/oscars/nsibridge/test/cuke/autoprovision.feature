Feature: set up oscars circuit when start time comes, tear down after end time

    Scenario: set up when start time arrives
        Given I have set up the run environment

        When I generate a reservation request
        When I set "start" time to 0 sec
        When I set "end" time to 120 sec

        When I assign random connId and corrId

        When I submit reserve

        When I assign a random corrId
        When I submit provision

        Then I wait up to 5000 ms until the "PSM" state is: "Provisioned"
        When I wait 1000 milliseconds
        Then the provMonitor has started "SETUP"

        When I assign a random corrId
        When I submit release

        Then I wait up to 5000 ms until the "PSM" state is: "Released"
        When I wait 1000 milliseconds
        Then the provMonitor has started "TEARDOWN"


