# =============================================================================
package TestManager;

=head1 NAME

TestManager - Perl test manager.

=head1 SYNOPSIS

  use TestManager;

=head1 DESCRIPTION

Handles tests.  TODO:  Merge with Test::Harness runtests, and have just one
instance of TestManager for all tests.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut

use vars qw($VERSION);
$VERSION = '0.1';

use XML::DOM;

use Data::Dumper;
use Error qw(:try);

use strict;

use OSCARS::Database;
use OSCARS::PluginManager;
use OSCARS::ClientManager;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
#
sub getParams {
    my( $self, $fname ) = @_;

    $self->{pluginMgr} = OSCARS::PluginManager->new();
    $self->{db} = OSCARS::Database->new();
    my $params = {};
    my $parser = new XML::DOM::Parser;
    my $doc = $parser->parsefile( $fname );
    my $testNodes = $doc->getElementsByTagName( "test" );
    my $n = $testNodes->getLength;
    for (my $i = 0; $i < $n; $i++) {
        my $testNode = $testNodes->item ($i);
        my $attr = $testNode->getAttributeNode( "name" );
	my $testName = $attr->getValue;
	# TODO:  error checking
        $params->{$testName} = {};
     	# iterate over the children of the test node (params)
        for my $paramNode ($testNode->getChildNodes) {
	    if ($paramNode->getNodeType() ne ELEMENT_NODE) { next; }
            $attr = $paramNode->getAttributeNode( "name" );
	    my $paramName = $attr->getValue;
            $attr = $paramNode->getAttributeNode( "value" );
	    $params->{$testName}->{$paramName} = $attr->getValue;
	}
    }
    return $params;
} #____________________________________________________________________________


###############################################################################
#
sub dispatch {
    my( $self, $params, $method ) = @_;

    if (!$self->{database}) {
        $self->{database} = $self->{pluginMgr}->getLocation('system');
        $self->{db}->connect($self->{database});
    }
    my $authN = $self->{pluginMgr}->usePlugin('authentication');
    my $credentials = $authN->getCredentials($params->{login}, 'password');
    $params->{method} = $method;
    $params->{password} = $credentials;
    if ( !$self->{clientMgr} ) {
        $self->{clientMgr} = OSCARS::ClientManager->new(
                                    'database' => $self->{database});
    }
    my $client = $self->{clientMgr}->getClient();
    my $som = $client->$method($params);
    if ($som->faultstring) { return( $som->faultstring, undef ); }
    return( undef, $som->result );
} #____________________________________________________________________________


###############################################################################
#
sub getReservationConfigs {
    my( $self, $testName ) = @_;

    if (!$self->{database}) {
        $self->{database} = $self->{pluginMgr}->getLocation('system');
        $self->{db}->connect($self->{database});
    }
    my $statement = "SELECT * FROM configTestAddresses a " .
        "INNER JOIN configTests t ON a.testConfId = t.id WHERE t.name = ?";
    my $rows = $self->{db}->doQuery($statement, $testName);
    my $configs = {};
    for my $row (@$rows) {
        $configs->{$row->{description}} = $row->{address};
    }
    return $configs;
} #____________________________________________________________________________


