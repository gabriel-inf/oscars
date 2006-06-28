#==============================================================================
package OSCARS::Library::Reservation::Common;

=head1 NAME

OSCARS::Library::Reservation::Common - Common functionality for reservations 

=head1 SYNOPSIS

  use OSCARS::Library::Reservation::Common;

=head1 DESCRIPTION

Common functionality for SOAP methods dealing with reservation creation, 
cancellation, and viewing.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 28, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

use OSCARS::Library::Reservation::TimeConversion;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{timeLib} = OSCARS::Library::Reservation::TimeConversion->new();
} #____________________________________________________________________________


###############################################################################
# details:  get reservation details from the database, given its
#     reservation id.  If a user has the proper authorization, he can view any 
#     reservation's details.  Otherwise he can only view reservations that
#     he has made, with less of the details.  If a database field is NULL
#     or blank, it is not returned.
#
# In:  reference to hash of parameters
# Out: reference to hash of reservation details
#
sub details {
    my( $self, $id ) = @_;

    my( $statement, $fields );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationAuthDetails WHERE id = ?';
        $fields = $self->{db}->getRow($statement, $id);
    }
    else {
        $statement = 'SELECT * FROM ReservationUserDetails ' .
                     'WHERE login = ? AND id = ?';
        $fields = $self->{db}->getRow($statement, $self->{user}->{login}, $id);
    }
    if (!$fields) { return undef; }
    my $results = $self->formatResults($fields);
    return $results;
} #____________________________________________________________________________


###############################################################################
# updateReservation: change the status of the reservervation from pending to
#                     active
#
sub updateReservation {
    my( $self, $resv, $status, $logger ) = @_;

    if ( !$resv->{lspStatus} ) {
        $resv->{lspStatus} = "Successful configuration";
        $status = $self->updateStatus($resv->{tag}, $status);
    } else { $status = $self->updateStatus($resv->{tag}, 'failed'); }
    $logger->info('updateReservation',
        { 'status' => $status, 'tag' => $resv->{tag} });
} #____________________________________________________________________________


###############################################################################
# formatResults:  Format results to be sent back from SOAP methods, given
#                fields from reservations table.
#
sub formatResults {
    my( $self, $fields ) = @_;

    my $pathArray;;

    my $results = {};
    $results->{tag} = $fields->{tag};
    $results->{startTime} = $self->{timeLib}->secondsToDatetime(
                              $fields->{startTime}, $fields->{origTimeZone});
    $results->{endTime} = $self->{timeLib}->secondsToDatetime(
                              $fields->{endTime}, $fields->{origTimeZone});
    $results->{createdTime} = $self->{timeLib}->secondsToDatetime(
                              $fields->{createdTime}, $fields->{origTimeZone});
    $results->{bandwidth} = $fields->{bandwidth};
    $results->{burstLimit} = $fields->{burstLimit};
    $results->{login} = $fields->{login};
    $results->{status} = $fields->{status};
    if ( $fields->{class} ) { $results->{class} = $fields->{class}; }
    if ( $fields->{srcPort} ) { $results->{srcPort} = $fields->{srcPort}; }
    if ( $fields->{destPort} ) { $results->{destPort} = $fields->{destPort}; }
    if ( $fields->{dscp} ) { $results->{dscp} = $fields->{dscp}; }
    if ( $fields->{protocol} ) { $results->{protocol} = $fields->{protocol}; }
    if ( $fields->{path} ) {
        $results->{path} = $fields->{path};
    }
    $results->{description} = $fields->{description};
    $results->{srcHost} = $fields->{srcHost};
    $results->{destHost} = $fields->{destHost};
    # The following field is only set if coming in from the scheduler
    if ( $fields->{lspStatus} ) {
        $results->{lspStatus} = $fields->{lspStatus};
        my $configTime = time();
        $results->{lspConfigTime} = $self->{timeLib}->secondsToDatetime(
                                         $configTime, $fields->{origTimeZone} );
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
#
sub getRouterName {
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
#
sub getPathInfo {
    my( $self, $pathId ) = @_;
 
    my $hops = $self->getPathHopAddresses($pathId);
    my $results = ();
    # FIX to be able to use id as well as IP
    for my $hop ( @{$hops} ) {
        my $routerName = $self->getRouterName(undef, $hop->{id});
        push(@$results, $routerName); 
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# hostIPToId:  get the primary key in the hosts table, given an
#     IP address.  A row is created if that address is not present.
# In:  hostIP
# Out: hostID
#
sub hostIPToId {
    my( $self, $ipaddr ) = @_;

    # TODO:  fix schema, possible hostIP would not be unique
    my $statement = 'SELECT id FROM hosts WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    # if no matches, insert a row in hosts
    if ( !$row ) {
        my $hostname = $self->ipToName($ipaddr);
        $statement = "INSERT INTO hosts VALUES (NULL, '$ipaddr', '$hostname')";
        $self->{db}->execStatement($statement);
        return $self->{db}->{dbh}->{mysql_insertid};
    }
    else { return $row->{id}; }
} #____________________________________________________________________________


###############################################################################
# hostIdToIP:  given the primary key in the hosts table, get the
#     host name.
# In:  hostID
# Out: hostIP
#
sub hostIdToIP {
    my( $self, $id ) = @_;

    my $statement = 'SELECT name, IP FROM hosts WHERE id = ?';
    my $row = $self->{db}->getRow($statement, $id);
    if ( $row->{name} ) { return $row->{name}; }
    else { return $row->{IP}; }
} #____________________________________________________________________________


###############################################################################
# nameToIP:  convert host name to IP address if it isn't already one
# In:   host name or IP, and whether to keep CIDR portion if IP address
# Out:  host IP address
#
sub nameToIP{
    my( $self, $host, $keepCidr ) = @_;

    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)*';
    # if doesn't match IP format, attempt to convert host name to IP address
    if ($host !~ $regexp) { return( inet_ntoa(inet_aton($host)) ); }
    elsif ($keepCidr) { return $host; }
    else { return $1; }   # return IP address without CIDR suffix
} #____________________________________________________________________________


###############################################################################
# getInterface:  Finds whether interface associated with IP address or host
#                name.
# IN:  IP address
# OUT: associated interface
#
sub getInterface {
    my( $self, $hop ) = @_;

    my $ipaddr = $self->nameToIP($hop);
    my $statement = 'SELECT interfaceId FROM topology.ipaddrs WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    return $row->{interfaceId};
} #____________________________________________________________________________


###############################################################################
# routerAddressType:  Gets trace or loopback IP address, if any, of router. 
# In:  IP address associated with router, and type of address to look from
# Out: alternative addres to do traceroute from, or loopback address, if any
#
sub routerAddressType {
    my( $self, $ipaddr, $addressType ) = @_;

    my $routerName = $self->getRouterName($ipaddr);
    if ( !$routerName ) { return undef; }

    # given router name, get address if any
    my $statement = 'SELECT IP FROM topology.ipaddrs ip ' .
        'INNER JOIN topology.interfaces i ON i.id = ip.interfaceId ' .
        'INNER JOIN topology.routers r ON r.id = i.routerId ' .
        "WHERE r.name = ? AND ip.description = '$addressType'";
    my $row = $self->{db}->getRow($statement, $routerName);
    return( $row->{IP} );
} #____________________________________________________________________________


#############
# Internal methods
#############

###############################################################################
# updateStatus: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub updateStatus {
    my ( $self, $tag, $status ) = @_;

    my @strArray = split('-', $tag);
    my $id = $strArray[-1];
    my $statement = qq{ SELECT status from reservations WHERE id = ?};
    my $row = $self->{db}->getRow($statement, $id);

    # If the previous state was pendingCancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pendingCancel.  The latter is used by 
    # FindExpiredReservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prevStatus = $row->{status};
    if ( ($prevStatus eq 'precancel') || ( ($prevStatus eq 'pending') &&
            ($status eq 'precancel'))) { 
        $status = 'cancelled';
    }
    $statement = qq{ UPDATE reservations SET status = ? WHERE id = ?};
    $self->{db}->execStatement($statement, $status, $id);
    return $status;
} #____________________________________________________________________________


###############################################################################
#
sub getPathHopAddresses {
    my( $self, $pathId ) = @_;
 
    my $statement = 'SELECT ip.IP, ip.description FROM topology.ipaddrs ip ' .
        'INNER JOIN pathIpaddrs pips ON pathId = ? ' .
        'WHERE pathId = ? ORDER BY sequenceNumber';
    my $hops = $self->{db}->doSelect($statement, $pathId);
    return $hops;
} #____________________________________________________________________________


###############################################################################
#
sub ipToName {
    my( $self, $ipaddr ) = @_;

    my $hostname;
    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)';
    # if doesn't match (not CIDR), attempt to get hostname
    if ($ipaddr !~ $regexp) {
        my $ip = inet_aton($ipaddr);
        $hostname = gethostbyaddr($ip, AF_INET);
    }
    if (!$hostname) { $hostname = $ipaddr; }
    return $hostname;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
