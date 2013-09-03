Feature: reservation records

    Scenario: verify a resv record is created
        Given I have set up the run environment

        When I assign random connId and corrId
        When I submit reserve
        Then I can find 1 resvRecord entries
        Then the resvRecord committed field is "false"
        Then I can "commit" the resvRecord entry


        When I assign random connId and corrId
        When I submit reserve
        Then I can find 1 resvRecord entries
        Then the resvRecord committed field is "false"
        Then I can "abort" the resvRecord entry

