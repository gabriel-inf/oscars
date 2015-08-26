Feature: provision a reservation

    I want to verify I can provision a new reservation

    Scenario: Provision, release
        Given I have set up the run environment
        Given I clear all existing tasks
        Given that I know the count of all pending provisioning requests
        When I generate a reservation request
        When I assign random connId and corrId

        When I submit reserve
        When I wait up to 15000 ms until I know the OSCARS gri

        When I assign a random corrId
        When I submit provision
        Then the last submit has not thrown an exception

        Then the "PSM" state is: "Provisioning"
        Then the count of pending provisioning requests has changed by 1

        Then I know the simpleRequest taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then the "PSM" state is: "Provisioned"

        When I assign a random corrId
        When I submit release
        Then the last submit has not thrown an exception

        Then the "PSM" state is: "Releasing"
        Then I know the simpleRequest taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then the "PSM" state is: "Released"
        Given I clear all existing tasks



    Scenario: Provision failure because of unknown connectionId
        Given I have set up the run environment
        Given I clear all existing tasks

        Given that I know the count of all pending provisioning requests
        When I assign random connId and corrId

        Given the count of ConnectionRecords is 0
        When I submit provision
        Then the last submit has thrown an exception
        Then the count of pending provisioning requests has changed by 0
        Given I clear all existing tasks

