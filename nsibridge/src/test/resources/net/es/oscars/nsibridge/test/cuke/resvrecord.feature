Feature: reservation records


    Scenario: verify commit sets the flag
        Given I have set up the run environment
        Given I clear all existing tasks

        When I assign random connId and corrId
        When I submit reserve
        Then the last submit has not thrown an exception
        Then I can find 1 resvRecord entries
        Then the resvRecord committed field is "false"

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"

        When I assign a random corrId
        When I submit reserveCommit
        Then the last submit has not thrown an exception
        Then I can find 1 resvRecord entries
        Then the resvRecord committed field is "true"
        Given I clear all existing tasks


    Scenario: verify abort removes the record
        Given I have set up the run environment
        Given I clear all existing tasks

        When I assign random connId and corrId
        When I submit reserve
        Then the last submit has not thrown an exception
        Then I can find 1 resvRecord entries
        Then the resvRecord committed field is "false"

        Then I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"

        When I assign a random corrId
        When I submit reserveAbort
        Then the last submit has not thrown an exception
        Then I can find 0 resvRecord entries
