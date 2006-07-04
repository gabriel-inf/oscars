#==============================================================================
package OSCARS::Library::Topology::Host;

=head1 NAME

OSCARS::Library::Topology::Host - Common functionality for hosts 

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Host;

=head1 DESCRIPTION

Common functionality for hosts.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;
use vars qw(@ISA);
@ISA = qw(OSCARS::Library::Topology::Address);

use Data::Dumper;
use Error qw(:try);
use Socket;

use OSCARS::Library::Topology::Address;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# toId:  get the primary key in the hosts table, given an IP address.  A row 
# is created in the hosts table if that address is not present.
# In:  hostIP
# Out: hostID
#
sub toId {
    my( $self, $ipaddr ) = @_;

    # TODO:  fix schema, possible hostIP would not be unique
    my $statement = 'SELECT id FROM hosts WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    # if no matches, insert a row in hosts
    if ( !$row ) {
        my $hostname = $self->toName($ipaddr);
        $statement = "INSERT INTO hosts VALUES (NULL, '$ipaddr', '$hostname')";
        $self->{db}->execStatement($statement);
        return $self->{db}->{dbh}->{mysql_insertid};
    }
    else { return $row->{id}; }
} #____________________________________________________________________________


###############################################################################
# toIP:  given the primary key in the hosts table, get the host name.
# In:  hostID
# Out: hostIP
#
sub toIP {
    my( $self, $id ) = @_;

    my $statement = 'SELECT name, IP FROM hosts WHERE id = ?';
    my $row = $self->{db}->getRow($statement, $id);
    if ( $row->{name} ) { return $row->{name}; }
    else { return $row->{IP}; }
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
