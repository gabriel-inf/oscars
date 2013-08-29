Feature: new reservation

  I want to verify I can create a new reservation

  Scenario: Submit new reservation internally through Java
    Given I have set up Spring
    Given I have stopped the scheduler
    Given I have started the Quartz scheduler


    Given that I know the count of all pending reservation requests

    When I set the current connId to: "reserve-connid"
    When I set the current corrId to: "reserve-submit-corrid"

    When I submit reserve
    Then the count of ConnectionRecords is 1
    Then the count of pending reservation requests has changed by 1
    Then the ReserveStateMachine state is: "ReserveChecking"
    Then the ResvRequest has OscarsOp: "RESERVE"

    Then I know the reserve taskIds
    When I tell the scheduler to run the taskIds in 500 milliseconds
    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then I know the OSCARS gri
    Then the ReserveStateMachine state is: "ReserveHeld"

    When I wait 1000 milliseconds
    When I set the current corrId to: "reserve-commit-corrid"

    When I submit reserveCommit
    Then I know the simpleRequest taskIds
    When I tell the scheduler to run the taskIds in 500 milliseconds
    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then the ReserveStateMachine state is: "ReserveStart"

    When I set the current corrId to: "reserve-submit-2-corrid"
    When I submit reserve
    Then the ReserveStateMachine state is: "ReserveChecking"
    When I wait 100 milliseconds

    Then I know the reserve taskIds
    When I tell the scheduler to run the taskIds in 500 milliseconds
    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then the ResvRequest has OscarsOp: "MODIFY"






  Scenario: Submit new reservation internally through Java (without a connectionId)
    When I set the current connId to: ""
    When I set the current corrId to: "reserve-no-connid"

    Given I have set up Spring
    Given that I know the count of all ConnectionRecords
    Given that I know the count of all pending reservation requests

    When I submit reserve
    Then the count of all ConnectionRecords has changed by 1
    Then the count of pending reservation requests has changed by 1


  Scenario: Exercise abort reservation
    Given I have set up Spring
    Given I have stopped the scheduler
    Given I have started the Quartz scheduler


    Given that I know the count of all pending reservation requests
    When I set the current connId to: "abort-connid"
    When I set the current corrId to: "abort-reserve-corrid"


    When I submit reserve
    Then the ReserveStateMachine state is: "ReserveChecking"
    Then the ResvRequest has OscarsOp: "RESERVE"

    Then I know the reserve taskIds
    When I tell the scheduler to run the taskIds in 500 milliseconds
    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then I know the OSCARS gri
    Then the ReserveStateMachine state is: "ReserveHeld"

    When I wait 1000 milliseconds

    When I set the current corrId to: "abort-reserve-corrid"
    When I submit reserveAbort
    Then the ReserveStateMachine state is: "ReserveAborting"
    Then I know the simpleRequest taskIds
    When I tell the scheduler to run the taskIds in 500 milliseconds
    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then the ReserveStateMachine state is: "ReserveStart"

