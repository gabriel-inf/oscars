Feature: inspect workflow tasks

    I want to verify I track the various tasks created

    Scenario: Reserve task tracking
        Given I have set up the run environment
        Given I clear all existing tasks
        When I generate a reservation request

        When I assign random connId and corrId


        When I submit reserve
        Then the "RSM" state is: "ReserveChecking"
        Then I know the reserve taskIds

        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then I know the OSCARS gri
        Then the "RSM" state is: "ReserveHeld"

        When I assign a random corrId
        When I submit reserveCommit
        Then I know the simpleRequest taskIds

        When I wait up to 1000 ms until the task runstate is "SCHEDULED"

        When I wait up to 10000 ms until the task runstate is "FINISHED"

        Then the "RSM" state is: "ReserveStart"
        Given I clear all existing tasks
