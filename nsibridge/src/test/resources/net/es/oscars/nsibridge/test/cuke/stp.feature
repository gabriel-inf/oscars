Feature: STP mapping reservation
    I want to verify that STPs are properly mapped

    Scenario: STP one-to-one mapping
      Given I have set up the run environment
      Given I have cleared all stp mapping config
      Given the source stp is "urn:ogf:network:es.net:2013:foo:bar"
      Given i set one stp mapping as "urn:ogf:network:es.net:2013:foo:bar" to "urn:ogf:network:es.net:xyy.zzz"
      When I perform all transforms and mappings
      Then the oscars urn is "urn:ogf:network:es.net:xyy.zzz"

    Scenario: STP transform
      Given I have set up the run environment
      Given I have cleared all stp mapping config
      Given the source stp is "urn:ogf:network:es.net:2013:foo:bar"
      Given i set the stp transform as match: "urn:ogf:network:es.net:2013" replace: "urn:ogf:network:es.net"
      When I perform all transforms and mappings
      Then the oscars urn is "urn:ogf:network:es.net:foo:bar"
      Given I clear all existing tasks
