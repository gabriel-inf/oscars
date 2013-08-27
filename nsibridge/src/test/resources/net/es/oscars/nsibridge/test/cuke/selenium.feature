Feature: Use the NSI requester web ui to submit requests

  Scenario: Submit new reservation through web browser
    Given I navigate to "http://nsi-requester.dlp.surfnet.nl/settings"
    Given I set the provider URL box to "foo"

