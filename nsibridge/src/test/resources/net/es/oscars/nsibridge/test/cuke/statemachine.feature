Feature: State machines behavior verification

    I want to verify my various state machines work as intended

    Scenario: No error walk through the Reserve uPA state machine
        Given I have set up the run environment

        When I assign random connId and corrId

        Given that I have created a new "RSM" state machine
        Given that I have set the "RSM" model implementation to be a stub

        Then the "RSM" state is: "ReserveStart"

        When I submit the "RSM" event: "RECEIVED_NSI_RESV_RQ"
        Then the "RSM" state is: "ReserveChecking"

        When I assign a random corrId
        When I submit the "RSM" event: "LOCAL_RESV_CHECK_CF"
        Then the "RSM" state is: "ReserveHeld"

        When I assign a random corrId
        When I submit the "RSM" event: "RECEIVED_NSI_RESV_CM"
        Then the "RSM" state is: "ReserveCommitting"

        When I assign a random corrId
        When I submit the "RSM" event: "LOCAL_RESV_COMMIT_CF"
        Then the "RSM" state is: "ReserveStart"
        Then the "RSM" has not thrown an exception





    Scenario: Exercise catching errors at the Reserve uPA state machine
        Given I have set up the run environment
        When I assign random connId and corrId

        Given that I have created a new "RSM" state machine
        Given that I have set the "RSM" model implementation to be a stub

        Then the "RSM" state is: "ReserveStart"
        When I submit the "RSM" event: "LOCAL_RESV_CHECK_CF"

        When I wait 100 milliseconds

        Then the "RSM" has thrown an exception
        Then the "RSM" state is: "ReserveStart"


    Scenario: No error walk through the Lifecycle uPA state machine
        Given I have set up the run environment
        When I assign random connId and corrId

        Given that I have created a new "LSM" state machine
        Given that I have set the "LSM" model implementation to be a stub
        Then the "LSM" state is: "Created"

        When I submit the "LSM" event: "RECEIVED_NSI_TERM_RQ"
        Then the "LSM" state is: "Terminating"

        When I assign a random corrId
        When I submit the "LSM" event: "LOCAL_TERM_CONFIRMED"
        Then the "LSM" state is: "Terminated"



  Scenario: No error walk through the Provisioning uPA state machine
        Given I have set up the run environment
        When I assign random connId and corrId

        Given that I have created a new "PSM" state machine
        Given that I have set the "PSM" model implementation to be a stub
        Then the "PSM" state is: "Released"

        When I assign a random corrId
        When I submit the "PSM" event: "RECEIVED_NSI_PROV_RQ"
        Then the "PSM" state is: "Provisioning"

        When I assign a random corrId
        When I submit the "PSM" event: "LOCAL_PROV_CONFIRMED"
        Then the "PSM" state is: "Provisioned"

        When I assign a random corrId
        When I submit the "PSM" event: "RECEIVED_NSI_REL_RQ"
        Then the "PSM" state is: "Releasing"

        When I assign a random corrId
        When I submit the "PSM" event: "LOCAL_REL_CONFIRMED"
        Then the "PSM" state is: "Released"
