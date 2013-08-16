Feature: State machines behavior verification

  I want to verify my various state machines work as intended

  Scenario: Walk through the Reserve uPA state machine
    Given that I have created a new ReserveStateMachine for connectionId: "reserve-sm"
    Given that I have set the Reserve model implementation to be a stub

    Then the ReserveStateMachine state is "ReserveStart"

    When I submit the Reserve event "LOCAL_RESV_CHECK_CF"
    Then the ReserveStateMachine has thrown an exception
    Then the ReserveStateMachine state is "ReserveStart"

    When I submit the Reserve event "RECEIVED_NSI_RESV_RQ"
    Then the ReserveStateMachine has not thrown an exception
    Then the ReserveStateMachine state is "ReserveChecking"

    When I submit the Reserve event "LOCAL_RESV_CHECK_CF"
    Then the ReserveStateMachine state is "ReserveHeld"

    When I submit the Reserve event "RECEIVED_NSI_RESV_CM"
    Then the ReserveStateMachine state is "ReserveCommitting"

    When I submit the Reserve event "LOCAL_RESV_COMMIT_CF"
    Then the ReserveStateMachine state is "ReserveStart"
