Feature: State machines behavior verification

  I want to verify my various state machines work as intended

  Scenario: No error walk through the Reserve uPA state machine
    Given that I have created a new ReserveStateMachine for connectionId: "reserve-sm"
    Given that I have set the Reserve model implementation for connectionId: "reserve-sm" to be a stub

    Then the ReserveStateMachine state for connectionId: "reserve-sm" is: "ReserveStart"

    When I submit the Reserve event: "RECEIVED_NSI_RESV_RQ" for connectionId: "reserve-sm"
    Then the ReserveStateMachine state for connectionId: "reserve-sm" is: "ReserveChecking"

    When I submit the Reserve event: "LOCAL_RESV_CHECK_CF" for connectionId: "reserve-sm"
    Then the ReserveStateMachine state for connectionId: "reserve-sm" is: "ReserveHeld"

    When I submit the Reserve event: "RECEIVED_NSI_RESV_CM" for connectionId: "reserve-sm"
    Then the ReserveStateMachine state for connectionId: "reserve-sm" is: "ReserveCommitting"

    When I submit the Reserve event: "LOCAL_RESV_COMMIT_CF" for connectionId: "reserve-sm"
    Then the ReserveStateMachine state for connectionId: "reserve-sm" is: "ReserveStart"
    Then the ReserveStateMachine for connectionId: "reserve-sm" has not thrown an exception





  Scenario: Exercise catching errors at the Reserve uPA state machine
    Given that I have created a new ReserveStateMachine for connectionId: "reserve-sm-fail"
    Given that I have set the Reserve model implementation for connectionId: "reserve-sm-fail" to be a stub
    Then the ReserveStateMachine state for connectionId: "reserve-sm-fail" is: "ReserveStart"

    When I submit the Reserve event: "LOCAL_RESV_CHECK_CF" for connectionId: "reserve-sm-fail"
    Then the ReserveStateMachine for connectionId: "reserve-sm-fail" has thrown an exception
    Then the ReserveStateMachine state for connectionId: "reserve-sm-fail" is: "ReserveStart"




  Scenario: No error walk through the Lifecycle uPA state machine
    Given that I have created a new LifecycleStateMachine for connectionId: "life-sm"
    Given that I have set the Lifecycle model implementation to be a stub
    Then the LifecycleStateMachine state is "Created"

    When I submit the Lifecycle event "RECEIVED_NSI_TERM_RQ"
    Then the LifecycleStateMachine state is "Terminating"

    When I submit the Lifecycle event "LOCAL_TERM_CONFIRMED"
    Then the LifecycleStateMachine state is "Terminated"



  Scenario: No error walk through the Provisioning uPA state machine
    Given that I have created a new ProvisioningStateMachine for connectionId: "prov-sm"
    Given that I have set the Provisioning model implementation to be a stub
    Then the ProvisioningStateMachine state is "Released"

    When I submit the Provisioning event "RECEIVED_NSI_PROV_RQ"
    Then the ProvisioningStateMachine state is "Provisioning"

    When I submit the Provisioning event "LOCAL_PROV_CONFIRMED"
    Then the ProvisioningStateMachine state is "Provisioned"

    When I submit the Provisioning event "RECEIVED_NSI_REL_RQ"
    Then the ProvisioningStateMachine state is "Releasing"

    When I submit the Provisioning event "LOCAL_REL_CONFIRMED"
    Then the ProvisioningStateMachine state is "Released"
