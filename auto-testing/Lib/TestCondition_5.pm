#!/usr/bin/perl
package Lib::TestCondition_5;
use Carp;
use Lib::Tester;
#use Lib::TopologyUtils;

use fields qw(NAME);

my $NAME = "TestCondition_5";

=head1


Topology Condition (5)
Two peering domains with EoMPLS network setting + eomplsPSS and Ethernet network setting + dragonPSS respectively;
Bottleneck intra- and inter-domain links; 
L2SC edge links with VLAN translation enabled and PSC trunk links

=cut


my $tester = new Lib::Tester;

# Seconds to sleep between checks for state changes
our $SLEEP = 30;

# Number of times to check for state changes
our $COUNT = 10;

# Start time can be 'no' or a number of minutes in the future.
#our $STARTTIME = 'now';
our $STARTTIME = 3;

# End time is reservation duration.
our $ENDTIME = '+00:00:10';


# Add circuits as needed
sub multi_test_5_1
{
	# Test Scenario (5.1)
	# specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : mixed translation and no-translation : inter-domain path

    my @arr;

    my %testParams_0 = (
        testName => $NAME . "_scenario_1",
        topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
        src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-8:link=link-1",
        srcVlan => "3",
        dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-8:link=link-1",
        dstVlan => "3",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
        expectedResult => "CANCELLED"
	);
    push @arr, \%testParams_0;

    my %testParams_1 = (
        testName => $NAME . "_scenario_1",
        topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
        src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-9:link=link-1",
        srcVlan => "5",
        dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-9:link=link-1",
        dstVlan => "5",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
        expectedResult => "CANCELLED"
	);
    push @arr, \%testParams_1;

    $tester->multi_test(@arr);
}


# Add circuits as needed
sub multi_test_5_2
{
	# Test Scenario (5.2)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : no-translation : inter-domain 

    my %testParams_0 = (
        testName => $NAME . "_scenario_1",
        topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
        src => "urn:ogf:network:domain=testdomain-2.net:node=node-3:port=port-9:link=link-1",
        srcVlan => "7",
        dst => "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-9:link=link-1",
        dstVlan => "7",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
        expectedResult => "CANCELLED"
	);
    push @arr, \%testParams_0;

    my %testParams_1 = (
        testName => $NAME . "_scenario_1",
        topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
        src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-9:link=link-1",
        srcVlan => "any",
        dst => "urn:ogf:network:domain=testdomain-3.net:node=node-4:port=port-9:link=link-1",
        dstVlan => "any",
        expectedResult => "CANCELLED"
	);
    push @arr, \%testParams_1;

    $tester->multi_test(@arr);
}


# Add circuits as needed
sub multi_test_5_3
{
	# Test Scenario (5.3)
	# specific_vlan_tag-to-specific_vlan_tag : simultaneous-execution-to-saturate : no-translation : single-hop-path-on-bottleneck-interdomain-link

    my %testParams_0 = (
        testName => $NAME . "_scenario_3",
        topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
        src => "urn:ogf:network:domain=testdomain-2.net:node=node-4:port=port-9:link=link-1",
        srcVlan => "8",
        dst => "urn:ogf:network:domain=testdomain-1.net:node=node-1:port=port-13:link=link-1",
        dstVlan => "8",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
        expectedResult => "CANCELLED"
	);
    push @arr, \%testParams_0;

    my %testParams_1 = (
        testName => $NAME . "_scenario_3",
        topology => "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml",
        src => "urn:ogf:network:domain=testdomain-2.net:node=node-1:port=port-11",
        srcVlan => "9",
        dst => "urn:ogf:network:domain=testdomain-3.net:node=node-1:port=port-13:link=link-1",
        dstVlan => "9",
        sleep => "$SLEEP",
        count => "$COUNT",
        startTime => "$STARTTIME",
        endTime => "$ENDTIME",
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
    $self->{NAME} = "TestCondition_5";
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

