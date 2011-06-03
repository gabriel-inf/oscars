#!/usr/bin/perl
package Lib::TestCondition_7;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);

my $NAME = "TestCondition_7";


=head1

Topology Condition (7)
Two peering domains with v0.6+EoMPLS network setting+eomplsPSS and v0.5.3+Ethernet network setting+dragonPSS respectively;
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut

my $tester = new Lib::Tester;


sub single_test_7_1
{
	# Test Scenario (7.1)
	# specific_vlan_tag-to-specific_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-2:link=link-1",
		srcVlan => "1000",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-3:link=link-1",
		dstVlan => "1000",
		expectedResult => "CANCELLED"
	);
	
	my $result = $tester->single_test(%testParams);
	$result;
}


sub single_test_7_2
{
	# Test Scenario (7.2)
	# any_vlan_tag-to-any_vlan_tag : v0.6-api-client-at-v0.5.3-domain : serial-execution : translation and no-translation : inter-domain path

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my %testParams = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-5:port=port-2:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-4.net:node=node-5:port=port-3:link=link-1",
		dstVlan => "any",
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
    $self->{NAME} = "TestCondition_7";
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

