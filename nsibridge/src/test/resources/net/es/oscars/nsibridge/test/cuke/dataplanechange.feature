Feature: verify dataplane change feature

    Scenario: perform complete reservation, check all dataplane change events
      Given I have set up the run environment
      Given I clear all existing tasks

      When I generate a reservation request
      When I set the version to 0
      When I set "start" time to 0 sec
      When I assign random connId and corrId

      When I submit reserve
      When I wait up to 10000 ms until the "RSM" state is: "ReserveHeld"
      When I assign a random corrId
      When I submit reserveCommit
      When I wait up to 10000 ms until the "RSM" state is: "ReserveStart"

      Then I can get the dataplane record with version 0
      Then the dataplane record "is not" active

      When I assign a random corrId
      When I submit provision

      Then I wait up to 5000 ms until the "PSM" state is: "Provisioned"
      When I wait up to 5000 ms until provMonitor schedules "SETUP"
      Given I wait up to 30 sec until any previous tasks complete


      Then I can get the dataplane record with version 0
      Then the dataplane record "is" active

      When I assign a random corrId
      When I submit terminate
      When I wait up to 10000 ms until the "LSM" state is: "Terminated"
      When I wait up to 10000 ms until the "PSM" state is: "Released"


      Then I can get the dataplane record with version 0
      Then the dataplane record "is not" active

      Given I clear all existing tasks
