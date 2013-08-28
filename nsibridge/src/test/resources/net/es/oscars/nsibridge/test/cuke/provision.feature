Feature: provision a reservation

  I want to verify I can provision a new reservation

  Scenario: Provision internally through Java
    Given I have set up Spring
    Given I have started the scheduler
    Given that I know the count of all pending provisioning requests
    When I submit reserve() with connectionId: "provision-connid"
    When I wait until I know the OSCARS gri for connectionId: "provision-connid"
    When I set the OSCARS stub state for connectionId: "provision-connid" to "RESERVED"

#    Given I have stopped the scheduler
#    Given I have started the Quartz scheduler


#    When I submit provision() with connectionId: "provision-connid"
#    Then I know the simpleRequest taskIds for connectionId: "provision-connid" type: "PROVISION"
#    When I tell the scheduler to run the taskIds for connectionId: "provision-connid" in 500 milliseconds
#    Then the count of pending provisioning requests has changed by 1
#    When I wait 500 milliseconds
#    Then the ProvisioningStateMachine state for connectionId: "provision-connid" is "Provisioning"

#    When I wait up to 10000 ms until the runstate for the taskIds for connectionId: "provision-connid" is "FINISHED"
#    Then the ProvisioningStateMachine state for connectionId: "provision-connid" is "Provisioned"



  Scenario: Provision failure because of unknown connectionId
    Given I have set up Spring
    Given that I know the count of all pending provisioning requests
    Given the count of ConnectionRecords with connectionId: "unknown-connid" is 0
    When I submit provision() with connectionId: "unknown-connid"
    Then the provision() call has thrown an exception
    Then the count of pending provisioning requests has changed by 0

