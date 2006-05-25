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

May 24, 2006

=cut

use vars qw($VERSION);
$VERSION = '0.1';

use XML::Simple;

use Data::Dumper;
use Error qw(:try);

use strict;

use NetLogger;

use OSCARS::Database;
use OSCARS::PluginManager;
use OSCARS::ClientManager;
use OSCARS::Logger;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    my $paramsMgr = OSCARS::PluginManager->new('location' => 'params.xml');
    $self->{params} = $paramsMgr->getConfiguration()->{test};
    my $configFile = $ENV{HOME} . '/.oscars.xml';
    my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
    $self->{dbconn} = OSCARS::Database->new();
    $self->{config} = $pluginMgr->getConfiguration();
    my $database = $self->{config}->{database}->{'system'}->{location};
    $self->{dbconn}->connect($database);
    $self->{authN} = $pluginMgr->usePlugin('authentication');
    $self->{clientMgr} = OSCARS::ClientManager->new(
                                    'database' => $database);
    $self->{logger} = OSCARS::Logger->new();
    $self->{logger}->setUserLogin('testaccount');
    $self->{logger}->set_level($NetLogger::INFO);
    $self->{logger}->open('test.log');
} #____________________________________________________________________________


###############################################################################
#
sub dispatch {
    my( $self, $methodName, $params ) = @_;

    my $methodParams = $self->{params}->{$methodName};
    if ( $params ) {
	for my $key ( keys %{ $methodParams } ) {
	    $params->{$key} = $methodParams->{$key};
	}
    }
    else { $params = $methodParams; }
    $self->{logger}->setMethod($methodName);
    my $info = Data::Dumper->Dump([$params], [qw(*REQUEST)]);
    $self->{logger}->info("request", { 'fields' => substr($info, 0, -1) });
    my $client = $self->{clientMgr}->getClient($methodName);

    my $method = SOAP::Data -> name($methodName)
        -> attr ( { 'xmlns' => $self->{config}->{namespace} } );
    my $request = SOAP::Data -> name($methodName . "Request" => $params );
    my $som = $client->call($method => $request);

    if ($som->faultstring) { $self->{logger}->warn( "Error", { 'fault' => $som->faultstring }); }
    else {
        $info = Data::Dumper->Dump([$som->result], [qw(*RESPONSE)]);
        $self->{logger}->info("response", { 'fields' => substr($info, 0, -1) });
    }
    if ($som->faultstring) { return( 0, undef ); }
    return( 1, $som->result );
} #____________________________________________________________________________


###############################################################################
#
sub getReservationConfigs {
    my( $self, $testName ) = @_;

    my $statement = "SELECT * FROM configTestAddresses a " .
        "INNER JOIN configTests t ON a.testConfId = t.id WHERE t.name = ?";
    my $rows = $self->{dbconn}->doSelect($statement, $testName);
    my $configs = {};
    for my $row (@$rows) {
        $configs->{$row->{description}} = $row->{address};
    }
    return $configs;
} #____________________________________________________________________________


sub close {
    my( $self ) = @_;

    $self->{dbconn}->disconnect();
} #____________________________________________________________________________

