Feature: provision a reservation

  I want to verify I can provision a new reservation

  Scenario: Provision internally through Java
    Given I have set up Spring
    Given that I know the count of all pending provisioning requests
    When I submit reserve() with connectionId: "provision-connid"
    When I submit provision() with connectionId: "provision-connid"
    Then the count of pending provisioning requests has changed by 1


  Scenario: Provision failure because of unknown connectionId
    Given I have set up Spring
    Given that I know the count of all pending provisioning requests
    Given the count of ConnectionRecords with connectionId: "unknown-connid" is 0
    When I submit provision() with connectionId: "unknown-connid"
    Then the provision() call has thrown an exception
    Then the count of pending provisioning requests has changed by 0

