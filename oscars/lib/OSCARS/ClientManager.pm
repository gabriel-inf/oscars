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

April 17, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

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
} #____________________________________________________________________________


###############################################################################
# getClient:  Adds SOAP::Lite client for given domain to clients 
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
    $self->{clients}->{$asNum} = SOAP::Lite
                                        -> uri($client->{uri})
                                        -> proxy($client->{proxy});
    return $self->{clients}->{$asNum};
} #____________________________________________________________________________


######
1;
