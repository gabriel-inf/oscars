@wip
Feature: miscellaneous tests

  Scenario: test DN normalization
    Given the incoming DN is "/C=US/ST=CA/L=Berkeley/O=ESnet/OU=ANTG/CN=MaintDB"
    Then the normalized DN matches "CN=MaintDB, OU=ANTG, O=ESnet, L=Berkeley, ST=CA, C=US"

    Given the incoming DN is "/OU=Domain Control Validated/CN=nsi-aggr-west.es.net"
    Then the normalized DN matches "CN=nsi-aggr-west.es.net, OU=Domain Control Validated"


    Given the incoming DN is "/C=US/ST=Arizona/L=Scottsdale/O=GoDaddy.com, Inc./OU=http://certs.godaddy.com/repository//CN=Go Daddy Secure Certificate Authority - G2"
    Then the normalized DN matches "CN=Go Daddy Secure Certificate Authority - G2, OU=http://certs.godaddy.com/repository/, O=GoDaddy.com\, Inc.,  L=Scottsdale, ST=Arizona, C=US"

    Given the incoming DN is "CN=Client, OU=NOT FOR PRODUCTION, O=ESNET, ST=CA, C=US"
    Then the normalized DN matches "CN=Client, OU=NOT FOR PRODUCTION, O=ESNET, ST=CA, C=US"

    Given the incoming DN is "CN=meican.cipo.rnp.br \<http://meican.cipo.rnp.br\>, ST=SP, C=BR, O=ICPEDU, OU=RNP, L=Campinas"
    Then the normalized DN matches "CN=meican.cipo.rnp.br \<http://meican.cipo.rnp.br\>, ST=SP, C=BR, O=ICPEDU, OU=RNP, L=Campinas"

    Given the incoming DN is "CN=ion.net.internet2.edu, OU=PlatinumSSL, OU=Internet2 NOC, O=Internet2, STREET=2737 E 10th St, L=Bloomington, ST=Indiana, OID.2.5.4.17=47408, C=US"
    Then the normalized DN matches "CN=ion.net.internet2.edu, OU=PlatinumSSL, OU=Internet2 NOC, O=Internet2, STREET=2737 E 10th St, L=Bloomington, ST=Indiana, OID.2.5.4.17=47408, C=US"

    Given the incoming DN is "CN=oscars/oscars.es.net,OU=Services,DC=ES,DC=net"
    Given the incoming DN is "CN=oscars/oscars.es.net,OU=Services,DC=ES,DC=net"
