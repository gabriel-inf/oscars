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

June 14, 2006

=cut

use vars qw($VERSION);
$VERSION = '0.1';

use XML::Simple;

use Data::Dumper;
use Error qw(:try);

use strict;

use NetLogger;

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
    $self->{config} = $pluginMgr->getConfiguration();
    $self->{clientMgr} = OSCARS::ClientManager->new(
                                  'configuration' => $self->{config}->{client});
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
	if ( $methodParams ) {
	    for my $key ( keys %{ $methodParams } ) {
	        $params->{$key} = $methodParams->{$key};
	    }
	}
    }
    elsif ( $methodParams ) { $params = $methodParams; }
    $self->{logger}->setMethod($methodName);
    print STDERR "method: $methodName\n";
    # special case for BNL
    if ($methodName eq 'testForward') {
        print STDERR "using password\n";
        $params->{password} =
            $self->{authN}->getCredentials($params->{login}, 'password');
        $ENV{WSS_SIGN} = 'false';
    }
    else {
        # sign using user's certificate
        $ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/usercert.pem";
        $ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/userkey.pem";
        # tells WSRF::Lite to sign the message with the above cert
        $ENV{WSS_SIGN} = 'true';
    }

    # if overriding actual method called
    if ( $params->{methodName} ) { $methodName = $params->{methodName}; }

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


sub close {
    my( $self ) = @_;

} #____________________________________________________________________________

