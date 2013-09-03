Feature: OSCARS operations

    Scenario: create an OSCARS reservation
        Given I have set up the run environment
        When I assign a random connId
        When submit a new OSCARS reserve() request
        Then I can save the new OSCARS GRI in a connectionRecord
        When I submit an OSCARS query
        Then I have saved an OSCARS state
        Then I can delete the ConnectionRecord
