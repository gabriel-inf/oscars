Feature: State machines behavior verification

  I want to verify my various state machines work as intended

  Scenario: No error walk through the Reserve uPA state machine
    When I set the current connId to: "reserve-sm"
    Given that I have created a new ReserveStateMachine
    Given that I have set the Reserve model implementation to be a stub

    Then the ReserveStateMachine state is: "ReserveStart"

    When I set the current corrId to: "reserve-sm-1"
    When I submit the Reserve event: "RECEIVED_NSI_RESV_RQ"
    Then the ReserveStateMachine state is: "ReserveChecking"

    When I set the current corrId to: "reserve-sm-2"
    When I submit the Reserve event: "LOCAL_RESV_CHECK_CF"
    Then the ReserveStateMachine state is: "ReserveHeld"

    When I set the current corrId to: "reserve-sm-3"
    When I submit the Reserve event: "RECEIVED_NSI_RESV_CM"
    Then the ReserveStateMachine state is: "ReserveCommitting"

    When I set the current corrId to: "reserve-sm-4"
    When I submit the Reserve event: "LOCAL_RESV_COMMIT_CF"

    Then the ReserveStateMachine state is: "ReserveStart"
    Then the ReserveStateMachine has not thrown an exception





  Scenario: Exercise catching errors at the Reserve uPA state machine
    When I set the current connId to: "reserve-sm-fail"
    When I set the current corrId to: "reserve-sm-fail"

    Given that I have created a new ReserveStateMachine
    Given that I have set the Reserve model implementation to be a stub

    Then the ReserveStateMachine state is: "ReserveStart"
    When I submit the Reserve event: "LOCAL_RESV_CHECK_CF"

    Then the ReserveStateMachine has thrown an exception
    Then the ReserveStateMachine state is: "ReserveStart"


# fixme to use helpers for connIds
  Scenario: No error walk through the Lifecycle uPA state machine
    Given that I have created a new LifecycleStateMachine for connectionId: "life-sm"
    Given that I have set the Lifecycle model implementation to be a stub
    Then the LifecycleStateMachine state is "Created"

    When I submit the Lifecycle event "RECEIVED_NSI_TERM_RQ" for correlationId: "life-corr-1"
    Then the LifecycleStateMachine state is "Terminating"

    When I submit the Lifecycle event "LOCAL_TERM_CONFIRMED" for correlationId: "life-corr-2"
    Then the LifecycleStateMachine state is "Terminated"



  Scenario: No error walk through the Provisioning uPA state machine
    Given that I have created a new ProvisioningStateMachine for connectionId: "prov-sm"
    Given that I have set the Provisioning model implementation for connectionId: "prov-sm" to be a stub
    Then the ProvisioningStateMachine state for connectionId: "prov-sm" is "Released"

    When I submit the Provisioning event "RECEIVED_NSI_PROV_RQ" for connectionId: "prov-sm" and correlationId: "prov-sm-corr-1"
    Then the ProvisioningStateMachine state for connectionId: "prov-sm" is "Provisioning"

    When I submit the Provisioning event "LOCAL_PROV_CONFIRMED" for connectionId: "prov-sm" and correlationId: "prov-sm-corr-2"
    Then the ProvisioningStateMachine state for connectionId: "prov-sm" is "Provisioned"

    When I submit the Provisioning event "RECEIVED_NSI_REL_RQ" for connectionId: "prov-sm" and correlationId: "prov-sm-corr-3"
    Then the ProvisioningStateMachine state for connectionId: "prov-sm" is "Releasing"

    When I submit the Provisioning event "LOCAL_REL_CONFIRMED" for connectionId: "prov-sm" and correlationId: "prov-sm-corr-4"
    Then the ProvisioningStateMachine state for connectionId: "prov-sm" is "Released"
