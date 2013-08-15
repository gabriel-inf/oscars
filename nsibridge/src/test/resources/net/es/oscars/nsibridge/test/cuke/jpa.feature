Feature: JPA insertion / deletion of records

  I want to verify I can insert and delete JPA records

  Scenario: Insert and find ConnectionRecord
    When I insert a new ConnectionRecord with id: "foobar"
    Then I can find the ConnectionRecord with id: "foobar"
