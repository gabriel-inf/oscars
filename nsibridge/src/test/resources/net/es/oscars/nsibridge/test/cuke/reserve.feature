Feature: new reservation

  I want to verify I can create a new reservation

  Scenario: Submit new reservation internally through Java
    Given I have set up Spring
    Given that I know the count of all pending reservation requests
    When I submit reserve() with connectionId: "reserve-connid"
    Then the count of ConnectionRecords with connectionId: "reserve-connid" is 1
    Then the count of pending reservation requests has changed by 1
    Given I have started the scheduler
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveChecking"
    Then the ResvRequest for connectionId: "reserve-connid" has OscarsOp: "RESERVE"
    When I wait until I know the OSCARS gri for connectionId: "reserve-connid"
    When I set the OSCARS stub state for connectionId: "reserve-connid" to "RESERVED"
    When I wait 2000 milliseconds
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveHeld"
    When I wait 500 milliseconds
    When I submit reserveCommit with connectionId: "reserve-connid"
    When I wait 500 milliseconds
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveStart"
    When I wait 500 milliseconds

    When I submit reserve() with connectionId: "reserve-connid"
    Then the ResvRequest for connectionId: "reserve-connid" has OscarsOp: "MODIFY"






  Scenario: Submit new reservation internally through Java (without a connectionId)
    Given I have set up Spring
    Given that I know the count of all ConnectionRecords
    Given that I know the count of all pending reservation requests
    When I submit reserve() with connectionId: ""
    Then the count of all ConnectionRecords has changed by 1
    Then the count of pending reservation requests has changed by 1

