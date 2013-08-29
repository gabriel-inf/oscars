Feature: provision a reservation

  I want to verify I can provision a new reservation

  Scenario: Provision internally through Java
    Given I have set up Spring
    Given I have started the scheduler
    Given that I know the count of all pending provisioning requests
    When I set the current connId to: "provision-connid"
    When I set the current corrId to: "provision-corrid-1"


    When I submit reserve
    When I wait until I know the OSCARS gri
    When I set the OSCARS stub state to "RESERVED"

#    Given I have stopped the scheduler
#    Given I have started the Quartz scheduler


#    When I submit provision
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
    When I set the current connId to: "unknown-connid"
    When I set the current corrId to: "unknown-corrid"

    Given the count of ConnectionRecords is 0
    When I submit provision
    Then the provision call has thrown an exception
    Then the count of pending provisioning requests has changed by 0

