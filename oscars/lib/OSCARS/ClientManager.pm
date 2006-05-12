# =============================================================================
package OSCARS::ClientManager;

=head1 NAME

OSCARS::ClientManager - SOAP client manager.

=head1 SYNOPSIS

  use OSCARS::ClientManager;

=head1 DESCRIPTION

Manages SOAP clients.  TODO:  Persistent instance, rather than one per
request.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 3, 2006

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

    $self->{clients} = {};
    #Points to the public key of the X509 certificate
    $ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/usercert.pem";
    #Points to the private key of the cert - must be unencrypted
    $ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/userkey.pem";
    #Tells WSRF::Lite to sign the message with the above cert
    $ENV{WSS_SIGN} = 'true';
} #____________________________________________________________________________


###############################################################################
# getClient:  Adds WSRF::Lite client for given domain to clients 
#              hash, indexed by autonomous system number.
#
sub getClient {
    my( $self, $asNum ) = @_;

    my( $statement, $client );

    if (!$asNum) { $asNum = 'local'; }
    if ($self->{clients}->{$asNum}) { return $self->{clients}->{$asNum}; }
    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    # currently only handles one server per domain
    if ($asNum ne 'local') {
        $statement = 'SELECT * FROM clients WHERE asNum = ?';
        $client = $dbconn->getRow($statement, $asNum);
    }
    else {
	# local domain not given an AS number in the clients table
        $statement = 'SELECT * FROM clients WHERE asNum IS NULL';
        $client = $dbconn->getRow($statement);
    }
    $dbconn->disconnect();
    if (!$client) { return undef; }
    $self->{clients}->{$asNum} = WSRF::Lite
                                        -> uri($client->{uri})
                                        -> proxy($client->{proxy});
    return $self->{clients}->{$asNum};
} #____________________________________________________________________________


######
1;
