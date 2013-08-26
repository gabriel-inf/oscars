Feature: provision a reservation

  I want to verify I can provision a new reservation

  Scenario: Provision internally through Java
    Given I have set up Spring
    Given I have started the scheduler
    Given that I know the count of all pending provisioning requests
    When I submit reserve() with connectionId: "provision-connid"
    When I wait until I know the OSCARS gri for connectionId: "provision-connid"
    When I set the OSCARS stub state for connectionId: "provision-connid" to "RESERVED"
    When I submit provision() with connectionId: "provision-connid"
    Then the count of pending provisioning requests has changed by 1
    When I wait 500 milliseconds
    Then the ProvisioningStateMachine state for connectionId: "provision-connid" is "Provisioning"



  Scenario: Provision failure because of unknown connectionId
    Given I have set up Spring
    Given that I know the count of all pending provisioning requests
    Given the count of ConnectionRecords with connectionId: "unknown-connid" is 0
    When I submit provision() with connectionId: "unknown-connid"
    Then the provision() call has thrown an exception
    Then the count of pending provisioning requests has changed by 0

