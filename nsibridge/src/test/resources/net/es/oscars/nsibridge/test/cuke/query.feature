Feature: query reservation

  Scenario: query a new reservation
    Given I have set up Spring
    When I submit reserve() with connectionId: "query-connid"
    Then querySummarySync() with connectionId: "query-connid" returns resvState "ReserveChecking"
