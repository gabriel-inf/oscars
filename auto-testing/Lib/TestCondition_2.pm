#!/usr/bin/perl
package Lib::TestCondition_2;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);


my $NAME = "TestCondition_2";

=head1

Topology Condition (2)
Single domain with EoMPLS network setting 
No common VLANs in path with sufficient bandwidth; 
L2SC edge links with VLAN translation enabled and PSC trunk links; 

=cut


my $tester = new Lib::Tester;


sub single_test_2_1
{
	# Test Scenario (2.1)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : no-translation

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-5:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-7:link=link-1",
		dstVlan => "3500",
		expectedResult => "FAILED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_2_2
{
	# Test Scenario (2.2)
	# specific_vlan_tag-to-specific_vlan_tag : serial-execution : translation (src_tag!=dst_tag)

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_2",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-4:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-6:link=link-1", 
		dstVlan => "3500",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_2_3
{
	# Test Scenario (2.3)
	# any_vlan_tag-to-any_vlan_tag : serial-executio

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-4:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-6:link=link-1", 
		dstVlan => "any",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_2_4
{
	# Test Scenario (2.4)
	# specific_vlan_tag-to-any_vlan_tag : serial-execution

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_4",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-4:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-6:link=link-1", 
		dstVlan => "any",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_2_5
{
	# Test Scenario (2.5)
	# any_vlan_tag-to-specific_vlan_tag : serial-execution

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_5",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-4:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-6:link=link-1", 
		dstVlan => "3500",
		expectedResult => "CANCELLED"
	);

	my $result = $tester->single_test(%testParams);
	$result;
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_2";
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

