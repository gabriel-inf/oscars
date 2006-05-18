# =============================================================================
package OSCARS::ClientManager;

=head1 NAME

OSCARS::ClientManager - SOAP client creator.

=head1 SYNOPSIS

  use OSCARS::ClientManager;

=head1 DESCRIPTION

Creates SOAP clients, using cached information from the clients table in the
oscars database.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

May 17, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

use WSRF::Lite;
use SOAP::Lite;

use OSCARS::Database;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    # cache information from the clients table
    $self->{clientInfo} = $self->getClientInfo();
    # points to the public key of the X509 certificate
    $ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/usercert.pem";
    # points to the private key of the cert - must be unencrypted
    $ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/userkey.pem";
    # tells WSRF::Lite to sign the message with the above cert
    $ENV{WSS_SIGN} = 'true';
} #____________________________________________________________________________


###############################################################################
# getClientInfo:  Caches information from clients table in oscars database.
#
sub getClientInfo {
    my( $self ) = @_;

    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    my $statement = 'SELECT * FROM clients';
    my $rows = $dbconn->doSelect($statement);
    $dbconn->disconnect();
    my $clientInfo = {};
    for my $row (@$rows) {
	my $domain = $row->{asNum};
	if (!$domain) { $domain = 'local'; }
	$clientInfo->{$domain} = {};
	$clientInfo->{$domain}->{uri} = $row->{uri};
	$clientInfo->{$domain}->{proxy} = $row->{proxy};
    }
    $clientInfo->{namespace} = 'http://oscars.es.net/OSCARS/Dispatcher';
    return $clientInfo;
} #____________________________________________________________________________


###############################################################################
# getClient:  Returns new WSRF::Lite client for given domain, with a SOAP
#             action set to the given method.
#
sub getClient {
    my( $self, $methodName, $domain ) = @_;

    # default is local domain
    if ( !$domain ) { $domain = 'local'; }
    my $soapAction = $self->{clientInfo}->{namespace} . '/' . $methodName;
    my $client = WSRF::Lite
        -> uri( $self->{clientInfo}->{$domain}->{uri} )
        -> proxy( $self->{clientInfo}->{$domain}->{proxy} )
	-> on_action ( sub { return "$soapAction" } );
    return $client;
} #____________________________________________________________________________


######
1;
