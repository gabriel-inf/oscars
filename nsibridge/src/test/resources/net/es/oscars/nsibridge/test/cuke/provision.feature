Feature: provision a reservation

  I want to verify I can provision a new reservation

  Scenario: Provision internally through Java
    Given I have set up the run environment
    Given that I know the count of all pending provisioning requests
    When I set the current connId to: "provision-connid"
    When I set the current corrId to: "provision-corrid-1"


    When I submit reserve
    When I wait until I know the OSCARS gri


    When I set the current corrId to: "provision-corrid-2"
    When I submit provision
    Then the last submit has not thrown an exception

    Then the "PSM" state is: "Provisioning"
    Then the count of pending provisioning requests has changed by 1

    Then I know the simpleRequest taskIds
    When I wait up to 10000 ms until the task runstate is "FINISHED"
    Then the "PSM" state is: "Provisioned"



  Scenario: Provision failure because of unknown connectionId
    Given I have set up the run environment
    Given that I know the count of all pending provisioning requests
    When I set the current connId to: "unknown-connid"
    When I set the current corrId to: "unknown-corrid"

    Given the count of ConnectionRecords is 0
    When I submit provision
    Then the last submit has thrown an exception
    Then the count of pending provisioning requests has changed by 0

