#!/usr/bin/perl
package Lib::TestCondition_3;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);


my $NAME = "TestCondition_3";

=head1

Topology Condition (3)
Two peering domains with EoMPLS network setting + eomplsPSS and Ethernet network setting + dragonPSS respectively; 
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut

my $tester = new Lib::Tester;




sub single_test_3_1
{
	# Test Scenario (3.1)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation : inter-domain

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-3:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-3:link=link-1",
		dstVlan => "1000",
		expectedResult => "CANCELLED"
	);

	my $result - $tester->single_test(%testParams);
	$result;
}


sub single_test_3_2
{
	# Test Scenario (3.2)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : translation (src_tag!=dst_tag) : inter-domain

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_2",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-2:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-2:link=link-1",
		dstVlan => "1100",
		expectedResult => "FAILED"
	);

	my $result - $tester->single_test(%testParams);
	$result;
}


sub single_test_3_3
{
	# Test Scenario (3.3)
	# any_vlan_tag-to-any_vlan_tag : serial-execution : inter-domain

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-2:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-2:link=link-1",
		dstVlan => "any",
		expectedResult => "CANCELLED"
	);

	my $result - $tester->single_test(%testParams);
	$result;
}


sub single_test_3_4
{
	# Test Scenario (3.4)
	# specific_vlan_tag-to-any_vlan_tag : serial-execution : inter-domain

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-2:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-2:link=link-1",
		dstVlan => "any",
		expectedResult => "CANCELLED"
	);

	my $result - $tester->single_test(%testParams);
	$result;
}


sub single_test_3_5
{
	# Test Scenario (3.5)
	# any_vlan_tag-to-specific_vlan_tag : serial-execution : inter-domain

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_5",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-2:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-2:link=link-1",
		dstVlan => "1000",
		expectedResult => "CANCELLED"
	);

	my $result - $tester->single_test(%testParams);
	$result;
}


sub single_test_3_6
{
	# Test Scenario (3.6)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : inter-domain : single-node-path-in-both-domains

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_6",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-10:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-1.net:node=node-1:port=port-11:link=link-1",
		dstVlan => "1000",
		expectedResult => "CANCELLED"
	);

	my $result - $tester->single_test(%testParams);
	$result;
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_3";
    bless($self);
    return $self;
}


sub name 
{
    my $self = shift;
    
    if ($Debugging) {
        carp "Debugging $self->{NAME}";
    }
    
    if (@_) { $self->{NAME} = shift }
    return $self->{NAME};
}

1;

