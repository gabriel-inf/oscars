Feature: JPA insertion / deletion of records

  I want to verify I can insert and delete JPA records

  Scenario: Insert, find, delete ConnectionRecord
    Given I have set up Spring
    When I set the current connId to: "foobar"

    Given the count of ConnectionRecords is 0
    When I insert a new ConnectionRecord
    Then the count of ConnectionRecords is 1
    Then I can delete the ConnectionRecord
    Then the count of ConnectionRecords is 0
