Feature: inspect workflow tasks

  I want to verify I track the various tasks created

  Scenario: Reserve task tracking
    Given I have set up Spring
    Given I have stopped the scheduler
    When I set the current connId to: "task-connid"
    When I set the current corrId to: "task-corrid-01"

    When I submit reserve
    Then the ReserveStateMachine state is: "ReserveChecking"

    Then I know the reserve taskIds
    Given I have started the Quartz scheduler

    When I tell the scheduler to run the taskIds in 500 milliseconds

    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then I know the OSCARS gri

    Then the ReserveStateMachine state is: "ReserveHeld"

    When I set the current corrId to: "task-corrid-02"
    When I submit reserveCommit
    Then I know the simpleRequest taskIds
    When I wait up to 1000 ms until the task runstate is "SCHEDULED"

    When I tell the scheduler to run the taskIds in 500 milliseconds
    When I wait up to 10000 ms until the task runstate is "FINISHED"

    Then the ReserveStateMachine state is: "ReserveStart"
