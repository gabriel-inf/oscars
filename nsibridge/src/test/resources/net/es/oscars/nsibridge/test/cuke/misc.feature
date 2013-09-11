Feature: miscellaneous tests
    Scenario: test DN normalization
        Given the incoming DN is "/C=US/ST=CA/L=Berkeley/O=ESnet/OU=ANTG/CN=MaintDB"
        Then the normalized DN is "CN=MaintDB, OU=ANTG, O=ESnet, L=Berkeley, ST=CA, C=US"

