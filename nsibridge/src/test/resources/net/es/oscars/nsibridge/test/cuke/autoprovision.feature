Feature: set up oscars circuit when start time comes

  Scenario: set up when start time arrives
      Given I have set up the run environment
      When I set the current connId to: "autoprov-connid"
      When I set the current corrId to: "autoprov-corrid-1"
      When I submit reserve
      When I set the current corrId to: "autoprov-corrid-2"
      When I submit provision
      Then the "PSM" state is: "Provisioning"
      When I wait 5000 milliseconds
      Then the "PSM" state is: "Provisioned"

