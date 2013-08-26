Feature: new reservation

  I want to verify I can create a new reservation

  Scenario: Submit new reservation internally through Java
    Given I have set up Spring
    Given I have stopped the scheduler
    Given I have started the Quartz scheduler


    Given that I know the count of all pending reservation requests

    When I submit reserve() with connectionId: "reserve-connid"

    Then the count of ConnectionRecords with connectionId: "reserve-connid" is 1
    Then the count of pending reservation requests has changed by 1
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveChecking"
    Then the ResvRequest for connectionId: "reserve-connid" has OscarsOp: "RESERVE"

    Then I know the reserve taskIds for connectionId: "reserve-connid"
    When I tell the scheduler to run the taskIds for connectionId: "reserve-connid" in 500 milliseconds
    When I wait up to 10000 ms until the runstate for the taskIds for connectionId: "reserve-connid" is "FINISHED"
    Then I know the OSCARS gri for connectionId: "reserve-connid"
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveHeld"

    When I wait 5000 milliseconds

    When I submit reserveCommit with connectionId: "reserve-connid"
    Then I know the simpleRequest taskIds for connectionId: "reserve-connid"
    When I tell the scheduler to run the taskIds for connectionId: "reserve-connid" in 500 milliseconds
    When I wait up to 10000 ms until the runstate for the taskIds for connectionId: "reserve-connid" is "FINISHED"
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveStart"


    When I submit reserve() with connectionId: "reserve-connid"
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveChecking"
    Then I know the reserve taskIds for connectionId: "reserve-connid"
    When I tell the scheduler to run the taskIds for connectionId: "reserve-connid" in 500 milliseconds
    When I wait up to 10000 ms until the runstate for the taskIds for connectionId: "reserve-connid" is "FINISHED"
    Then the ResvRequest for connectionId: "reserve-connid" has OscarsOp: "MODIFY"






  Scenario: Submit new reservation internally through Java (without a connectionId)
    Given I have set up Spring
    Given that I know the count of all ConnectionRecords
    Given that I know the count of all pending reservation requests
    When I submit reserve() with connectionId: ""
    Then the count of all ConnectionRecords has changed by 1
    Then the count of pending reservation requests has changed by 1

