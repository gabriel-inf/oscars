Feature: query reservation

  Scenario: query a new reservation
    Given I have set up the run environment

    When I set the current connId to: "query-connid"
    When I set the current corrId to: "query-corrid-1"

    When I submit reserve
    When I set the current corrId to: "query-corrid-2"
    Then querySummarySync() returns resvState "ReserveChecking"
