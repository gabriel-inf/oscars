#!/usr/bin/perl
package Lib::Simulator;
use Carp;
use Lib::Tester;
use Lib::TopologyUtils;

use fields qw(NAME);


my $NAME = "Sumulator";



=head1

=cut

my $tester = new Lib::Tester;


sub create 
{
	_do_test("_create", 30);
}


sub _do_test
{
	my $t_name = shift;
	my $iterations = shift;

	my $topologyFile = "/usr/local/oscars/TopoBridgeService/conf/testdomain-2.net.xml";
	my $parser = new Lib::TopologyUtils;
	my @topology = $parser->parseV6(fileName => $topologyFile);
	my $n = @topology;
	my $c = 0;
	my $src; 
	my $dst; 
	my %pSrc;
	my %pDst;
	my $endMins;
	my $endTime;

	srand(1000);

	while ($c < $iterations) {
		my $r = int(rand($n));		
		$src = $topology[$r]->{'linkId'};
		%pSrc = $parser->parse_urn(urn => $src); 

		$r = int(rand($n));		
		$dst = $topology[$r]->{'remoteLinkId'};
		%pDst = $parser->parse_urn(urn => $dst);
	
		next if ($src eq $dst);
		next if ($pSrc{'domain'} ne $pDst{'domain'});
		next if ($pSrc{'node'} eq '*');
		next if ($pDst{'node'} eq '*');

		#$endMins = 10 + int(rand(59 - 10));
		$endMins = 10 + int(rand(15 - 10));
		$endTime = "+00:00:" . $endMins;

	    my %testParams = (
        	testName => $NAME . $t_name,
	        topology => $topologyFile, 
   	    	src => $src,
        	srcVlan => 'any', 
        	dst => $dst,
        	dstVlan => 'any',
			endTime => $endTime,	
        	expectedResult => "ACTIVE"
   		);
		#print map { "$_ => $testParams{$_}\n" } sort keys %testParams;
		#print "\n";

    	$tester->create(%testParams);
		$c++;
		if ($c != $iterations) {
			#my $sleepTime = 1 + int(rand(15 * 60));
			my $sleepTime = 1 + int(rand(2 * 60));
			print "sleeping ...\n";
			sleep($sleepTime);
		}
	}
}


sub new()
{
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my $self = fields::new($class);
    $self->{NAME} = "";
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

