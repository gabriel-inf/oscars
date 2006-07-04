#==============================================================================
package OSCARS::Library::Topology::Router;

=head1 NAME

OSCARS::Library::Topology::Router - Functionality for router handling 

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Router;

=head1 DESCRIPTION

Functionality dealing with router handling.

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

use OSCARS::Library::Topology::JnxSNMP;
use OSCARS::Library::Topology::Address;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{jnxSnmp} = OSCARS::Library::Topology::JnxSNMP->new(
                                                     'db' => $self->{db} );
} #____________________________________________________________________________


###############################################################################
#
sub name {
    my( $self, $ipaddr, $ipaddrId ) = @_;
 
    my $row;

    my $statement = 'SELECT r.name FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId ' .
        'INNER JOIN topology.ipaddrs ip ON i.id = ip.interfaceId';
    if ($ipaddrId) {
        $statement .= ' WHERE ip.id = ?';
        $row = $self->{db}->getRow($statement, $ipaddrId);
    }
    else { 
        $statement .= ' WHERE ip.IP = ?';
        $row = $self->{db}->getRow($statement, $ipaddr);
    }
    return $row->{name};
} #____________________________________________________________________________


###############################################################################
# interface:  Finds whether interface associated with IP address or host
#                name.
# IN:  IP address
# OUT: associated interface
#
sub interface {
    my( $self, $hop ) = @_;

    my $ipaddr = $self->nameToIP($hop);
    my $statement = 'SELECT interfaceId FROM topology.ipaddrs WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    return $row->{interfaceId};
} #____________________________________________________________________________


###############################################################################
# info:  Gets trace or loopback IP address, if any, of router. 
# In:  IP address associated with router, and type of address to look from
# Out: alternative addres to do traceroute from, or loopback address, if any
#
sub info {
    my( $self, $ipaddr, $addressType ) = @_;

    my $routerName = $self->name($ipaddr);
    if ( !$routerName ) { return undef; }

    # given router name, get address if any
    my $statement = 'SELECT IP FROM topology.ipaddrs ip ' .
        'INNER JOIN topology.interfaces i ON i.id = ip.interfaceId ' .
        'INNER JOIN topology.routers r ON r.id = i.routerId ' .
        "WHERE r.name = ? AND ip.description = '$addressType'";
    my $row = $self->{db}->getRow($statement, $routerName);
    return( $row->{IP} );
} #____________________________________________________________________________


###############################################################################
# queryDomain:  Gets the autonomous service number associated with an IP 
#     address by performing an SNMP query against the egress router
#                
# In:  IP address of egress router, IP address of next hop
# Out: autonomous service number (used by the AAAS to look up uri and proxy)
#
sub queryDomain {
    my( $self, $interfaceIP, $ipaddr ) = @_;

    my $asNumber;

    # Get router name for logging.  If unable to get, router not in db
    my $routerName = $self->name( $interfaceIP );
    if ( !$routerName ) {
        throw Error::Simple("Pathfinder.getAsNumber: no router in database for $interfaceIP");
    }
    $self->{jnxSnmp}->initializeSession( $routerName );
    my $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        throw Error::Simple("Unable to initialize SNMP session: $errorMsg");
    }
    $asNumber = $self->{jnxSnmp}->queryAsNumber($ipaddr);
    $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        #Log SNMP failure but build reservation up to this point
        $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
              'errorMessage' => $errorMsg });
        $asNumber = 'noSuchInstance';
    }
    else {
        $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
            'AS' => $asNumber });
    }
    return $asNumber;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
