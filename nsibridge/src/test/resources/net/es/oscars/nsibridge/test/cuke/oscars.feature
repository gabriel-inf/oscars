Feature: OSCARS operations

  Scenario: create an OSCARS reservation
    Given I have set up Spring
    Given I have set up the OSCARS proxy
    When I set the current connId to: "oscars-resv"

    When submit a new OSCARS reserve() request
    Then I can save the new OSCARS GRI in a connectionRecord
    When I submit an OSCARS query
    Then I have saved an OSCARS state
