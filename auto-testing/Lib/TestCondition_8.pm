#!/usr/bin/perl
package Lib::TestCondition_8;
use Carp;
use Lib::Tester;
use Lib::TopologyUtils;

use fields qw(NAME);


my $NAME = "TestCondition_8";



=head1

Topology Condition (8)
Three peering domains in linear topology, two configured with EoMPLS network setting+eomplsPSS and on with v0.5.3+Ethernet network setting+dragonPSS respectively;
Common VLAN in path with sufficient bandwidth for both intra- and inter-domain links;
L2SC edge links with VLAN translation enabled and PSC trunk links


=cut

my $tester = new Lib::Tester;


sub multi_test_8_1
{
	# Test Scenario (8.1)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : translation and no-translation : intra-domain and inter-domain paths : continue-random-execution-and-monito

	_do_test("_scenario_1", 10);
}


sub multi_test_8_2
{
	# Test Scenario (8.2)
	# mixed specific_vlan_tag-to-specific_vlan_tag and any_vlan_tag-to-any_vlan_tag : simultaneous-execution-to-saturate : translation and no-translation : intra-domain and inter-domain paths : :schedule-for-ten-thousand-circuits : continue-random-execution-and-monitor  
	
	_do_test("_scenario_2", 10000);
}


sub _do_test
{
	my $self = shift;
	my $t_name = shift;
	my $iterations = shift;

	# Additional optional parameters are:
	# layer, bandwidth, start-time, end-time, path-setup-mode
    my @arr;
	my $topologyFile = "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml";
	my $parser = new Lib::TopologyUtils;
	my @topology = $parser->parseV6(fileName => $topologyFile);
	my $n = @topology;
	my $limit = 4000;
	my $c = 0;
	my $src; 
	my $dst; 
	my $srcVlan; 
	my $dstVlan;
	my $endTime;

	while ($c < $iterations) {
		my $r = int(rand($n));		
		$src = $topology[$r]->{'linkId'};
		$r = int(rand($n));		
		$dst = $topology[$r]->{'remoteLinkId'};
		if ($src eq $dst) {	
			next;
		}
		$srcVlan = int(rand($limit));
		$dstVlan = int(rand($limit));
		$endTime = int(rand(25));

	    my %testParams = (
        	testName => $NAME . $t_name,
	        topology => $topologyFile, 
   	    	src => $src,
        	srcVlan => $srcVlan, 
        	dst => $dst,
        	dstVlan => $dstVlan,
			endTime => $endTime,	
        	expectedResult => "CANCELLED"
   		);
    	push @arr, \%testParams_0;
		$c++;
	}

    $tester->multi_test(@arr);
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "TestCondition_8";
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

