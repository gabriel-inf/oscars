#!/usr/bin/perl
package Lib::TestCondition_4;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);

my $NAME = "TestCondition_4";


=head1

Topology Condition (4)
Single domain with EoMPLS network setting 
Bottleneck link in path with very small bandwidth and very limited number of VLANs;
L2SC edge links with VLAN translation enabled and PSC trunk links; 

=cut

my $tester = new Lib::Tester;


# Add reservations as needed
sub multi_test_4_1
{
	# Test Scenario (4.1)
	# specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : mixed translation and no-translation

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

	my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-8:link=link-1",
		srcVlan => "3",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-8:link=link-1",
		dstVlan => "3",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "5",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-9:link=link-1",
		dstVlan => "5",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;

	$tester->multi_test(@arr);
}


# Add reservations as needed
sub multi_test_4_2
{
	# Test Scenario (4.2)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : no-translation

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

    my @arr;

	my %testParams_0 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "any",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-9:link=link-1",
		dstVlan => "any",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_0;

	my %testParams_1 = (
		testName => $NAME . "_scenario_1",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "5",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-2:port=port-9:link=link-1",
		dstVlan => "5",
		expectedResult => "CANCELLED"
	);
	push @arr, \%testParams_1;

    $tester->multi_test(@arr);
}


# Add reservations as needed
sub multi_test_4_3
{
	# Test Scenario (4.3)
	# specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : no-translation : single-node-path with one end on the bottleneck link

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode

    my @arr;

    my %testParams_0 = (
        testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "10",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-11:link=link-1",
		dstVlan => "10",
        expectedResult => "CANCELLED"
    );
    push @arr, \%testParams_0;

    my %testParams_1 = (
        testName => $NAME . "_scenario_3",
		topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
		src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-9:link=link-1",
		srcVlan => "15",
		dst => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-11:link=link-1",
		dstVlan => "15",
        expectedResult => "CANCELLED"
    );
    push @arr, \%testParams_1;

    $tester->multi_test(@arr);
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_4";
    bless($self);
    return $self;
}


sub name 
{
    my $self = shift;
    
    if ($Demo) {
        carp "Demo $self->{NAME}";
    }
    
    if (@_) { $self->{NAME} = shift }
    return $self->{NAME};
}

1;

