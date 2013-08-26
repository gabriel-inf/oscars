Feature: inspect workflow tasks

  I want to verify I track the various tasks created

  Scenario: Reserve task tracking
    Given I have set up Spring
    Given I have stopped the scheduler
    When I submit reserve() with connectionId: "task-connid"
    Then the ReserveStateMachine state for connectionId: "task-connid" is: "ReserveChecking"

    Then I know the taskIds for connectionId: "task-connid"
    Given I have started the Quartz scheduler

    When I tell the scheduler to run the taskIds for connectionId: "task-connid" in 500 milliseconds

    When I wait up to 15000 ms until the runstate for the taskIds for connectionId: "task-connid" is "FINISHED"

    Then the ReserveStateMachine state for connectionId: "task-connid" is: "ReserveHeld"
    Given I have stopped the Quartz scheduler
