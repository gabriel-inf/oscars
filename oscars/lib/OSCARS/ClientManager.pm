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
# get_client:  Adds SOAP::Lite client for given domain to clients 
#              hash, indexed by autonomous system number.
#
sub get_client {
    my( $self, $as_num ) = @_;

    my( $statement, $client );

    if (!$as_num) { $as_num = 'local'; }
    if ($self->{clients}->{$as_num}) { return $self->{clients}->{$as_num}; }
    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    # currently only handles one server per domain
    if ($as_num ne 'local') {
        $statement = 'SELECT * FROM clients WHERE as_num = ?';
        $client = $dbconn->get_row($statement, $as_num);
    }
    else {
	# local domain not given an AS number in the clients table
        $statement = 'SELECT * FROM clients WHERE as_num IS NULL';
        $client = $dbconn->get_row($statement);
    }
    $dbconn->disconnect();
    if (!$client) { return undef; }
    $self->{clients}->{$as_num} = SOAP::Lite
                                        -> uri($client->{client_uri})
                                        -> proxy($client->{client_proxy});
    return $self->{clients}->{$as_num};
} #____________________________________________________________________________


######
1;
