Feature: new reservation

  I want to verify I can create a new reservation

  Scenario: Submit new reservation internally through Java
    Given that I have submitted reserve() with connectionId: "abcdef"
    Then I can find the ConnectionRecord with id: "abcdef"

  Scenario: Submit new reservation internally through Java without a connectionId
    Given that I know the count of all ConnectionRecords
    Given that I have submitted reserve() with connectionId: ""
    Then the count of ConnectionRecords has increased by 1