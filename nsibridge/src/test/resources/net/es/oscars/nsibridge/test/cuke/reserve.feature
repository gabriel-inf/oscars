Feature: new reservation

    I want to verify I can create a new reservation

    Scenario: Submit new reservation internally through Java
        Given I have set up the run environment

        When I generate a reservation request

        Given that I know the count of all pending reservation requests

        When I assign random connId and corrId

        When I submit reserve
        Then the last submit has not thrown an exception
        Then the count of ConnectionRecords is 1
        Then the count of pending reservation requests has changed by 1
        Then the "RSM" state is: "ReserveChecking"
        Then the ResvRequest has OscarsOp: "RESERVE"

        Then I know the reserve taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then I know the OSCARS gri
        Then the "RSM" state is: "ReserveHeld"


        When I assign a random corrId
        When I submit reserveCommit
        Then the last submit has not thrown an exception
        Then I know the simpleRequest taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then the "RSM" state is: "ReserveStart"

        When I assign a random corrId
        When I submit reserve
        Then the "RSM" state is: "ReserveChecking"
        When I wait 100 milliseconds

        Then I know the reserve taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then the ResvRequest has OscarsOp: "MODIFY"
        # only because modify is not fully implemented
        Then the "RSM" state is: "ReserveFailed"


    Scenario: Submit new reservation internally through Java (without a connectionId)
        Given I have set up the run environment
        When I set the current connId to: ""
        When I assign a random corrId

        Given I have set up the run environment

        Given that I know the count of all ConnectionRecords
        Given that I know the count of all pending reservation requests

        When I generate a reservation request

        When I submit reserve
        Then the count of all ConnectionRecords has changed by 1
        Then the count of pending reservation requests has changed by 1




    Scenario: Exercise abort reservation
        Given I have set up the run environment


        Given that I know the count of all pending reservation requests
        When I assign random connId and corrId

        When I generate a reservation request

        When I submit reserve
        Then the "RSM" state is: "ReserveChecking"
        Then the ResvRequest has OscarsOp: "RESERVE"

        Then I know the reserve taskIds
        When I wait up to 10000 ms until the task runstate is "FINISHED"
        Then I know the OSCARS gri
        Then the "RSM" state is: "ReserveHeld"

        When I wait 1000 milliseconds

        When I assign a random corrId
        When I submit reserveAbort
        Then I wait up to 5000 ms until the "RSM" state is: "ReserveStart"


