Feature: JPA insertion / deletion of records

  I want to verify I can insert and delete JPA records

  Scenario: Insert, find, delete ConnectionRecord
    Given the count of ConnectionRecords with connectionId: "foobar" is 0
    When I insert a new ConnectionRecord with connectionId: "foobar"
    Then the count of ConnectionRecords with connectionId: "foobar" is 1
    Then I can delete the ConnectionRecord with connectionId: "foobar"
    Then the count of ConnectionRecords with connectionId: "foobar" is 0
