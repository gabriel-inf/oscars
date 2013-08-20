Feature: OSCARS operations

  Scenario: create an OSCARS reservation
    Given I have set up Spring
    Given I have set up the OSCARS proxy
    When submit a new OSCARS reserve() request
    Then I can save the new OSCARS GRI in a connectionRecord for connectionId: "oscars-resv"
    When I submit an OSCARS query for connectionId: "oscars-resv"
    Then I have saved an OSCARS state for connectionId: "oscars-resv"
