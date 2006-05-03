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

May 2, 2006

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
# listDetails:  get reservation details from the database, given its
#     reservation id.  If a user has the proper authorization, he can view any 
#     reservation's details.  Otherwise he can only view reservations that
#     he has made, with less of the details.
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub listDetails {
    my( $self, $params ) = @_;

    my( $statement, $results );

    my @strArray = split('-', $params->{tag});
    my $id = $strArray[-1];
    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationAuthDetails WHERE id = ?';
        $results = $self->{db}->getRow($statement, $id);
        my $pathArray = $self->getPathRouterInfo($results->{path});
        $results->{path} = join(' ', @{$pathArray});
    }
    else {
        $statement = 'SELECT * FROM ReservationUserDetails ' .
                     'WHERE login = ? AND id = ?';
        $results = $self->{db}->getRow($statement, $self->{user}->{login}, $id);
    }
    if (!$results) { return undef; }
    $self->checkNulls($results);
    ( $results->{ingressRouterIP}, $results->{ingressLoopbackIP} ) = $self->getRouterInfo(
                              $results->{ingressInterfaceId});
    ( $results->{egressRouterIP}, $results->{egressLoopbackIP} ) = $self->getRouterInfo(
                              $results->{egressInterfaceId});
    $results->{startTime} = $self->{timeLib}->secondsToDatetime(
                              $results->{startTime}, $results->{origTimeZone});
    $results->{endTime} = $self->{timeLib}->secondsToDatetime(
                              $results->{endTime}, $results->{origTimeZone});
    $results->{createdTime} = $self->{timeLib}->secondsToDatetime(
                              $results->{createdTime}, $results->{origTimeZone});
    return $results;
} #____________________________________________________________________________


###############################################################################
# updateReservation: change the status of the reservervation from pending to
#                     active
#
sub updateReservation {
    my ($self, $resv, $status, $logger) = @_;

    if ( !$resv->{lspStatus} ) {
        $resv->{lspStatus} = "Successful configuration";
        $status = $self->updateStatus($resv->{tag}, $status);
    } else { $status = $self->updateStatus($resv->{tag}, 'failed'); }
    $logger->info('updateReservation',
        { 'status' => $status, 'tag' => $resv->{tag} });
} #____________________________________________________________________________


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
sub getRouterInfo {
    my( $self, $interfaceId ) = @_;
 
    my $statement =
        qq{SELECT name, loopback FROM topology.routers WHERE id =
           (SELECT routerId FROM topology.interfaces WHERE topology.interfaces.id = ?)};
    # TODO:  FIX row might be empty
    my $row = $self->{db}->getRow($statement, $interfaceId);
    return( $row->{name}, $row->{loopback} );
} #____________________________________________________________________________


###############################################################################
#
sub getPathRouterInfo {
    my( $self, $path ) = @_;
 
    my @pathRouters = split(' ', $path);
    my $results = ();
    for my $interfaceId (@pathRouters) {
        my( $name, $loopback ) = $self->getRouterInfo($interfaceId);
        push(@$results, $name); 
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

    my $hostname;

    # TODO:  fix schema, possible hostIP would not be unique
    my $statement = 'SELECT id FROM hosts WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    # if no matches, insert a row in hosts
    if ( !$row ) {
        # first group tests for IP address, second handles CIDR blocks
        my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)';
        # if doesn't match (not CIDR), attempt to get hostname
        if ($ipaddr !~ $regexp) {
            my $ip = inet_aton($ipaddr);
            $hostname = gethostbyaddr($ip, AF_INET);
        }
        if (!$hostname) { $hostname = $ipaddr; }
        $statement = "INSERT INTO hosts VALUES (NULL, '$ipaddr', '$hostname')";
        $self->{db}->execStatement($statement);
        return $self->{db}->{dbh}->{mysql_insertid};
    }
    else { return $row->{id}; }
} #____________________________________________________________________________


###############################################################################
# checkNulls:  
#
sub checkNulls {
    my( $self, $resv ) = @_ ;

    # clean up NULL values
    if (!$resv->{protocol} || ($resv->{protocol} eq 'NULL')) {
        $resv->{protocol} = 'DEFAULT';
    }
    if (!$resv->{dscp} || ($resv->{dscp} eq 'NU')) {
        $resv->{dscp} = 'DEFAULT';
    }
} #____________________________________________________________________________


###############################################################################
# reservationStats
#
sub reservationStats {
    my( $self, $resv) = @_;

    # TODO:  FIX! infiniteTime
    my $infiniteTime = 'foo';
    # only optional fields need to be checked for existence
    my $msg = "Description:        $resv->{description}\n";
    if ($resv->{tag}) { $msg .= "Reservation tag:     $resv->{tag}\n"; }

    $msg .= "Start time:         $resv->{startTime}\n";
    if ($resv->{endTime} ne $infiniteTime) {
        $msg .= "End time:           $resv->{endTime}\n";
    }
    else { $msg .= "End time:           persistent circuit\n"; }

    if ($resv->{createdTime}) {
        $msg .= "Created time:       $resv->{createdTime}\n";
    }
    $msg .= "(Times are in UTC $resv->{origTimeZone})\n";
    $msg .= "Bandwidth:          $resv->{bandwidth}\n";
    if ($resv->{burstLimit}) {
        $msg .= "Burst limit:         $resv->{burstLimit}\n";
    }
    $msg .= "Source:             $resv->{srcHost}\n" .
        "Destination:        $resv->{destHost}\n";
    if ($resv->{srcPort}) {
        $msg .= "Source port:        $resv->{srcPort}\n";
    }
    else { $msg .= "Source port:        DEFAULT\n"; }

    if ($resv->{destPort}) {
        $msg .= "Destination port:   $resv->{destPort}\n";
    }
    else { $msg .= "Destination port:   DEFAULT\n"; }

    $msg .= "Protocol:           $resv->{protocol}\n";
    $msg .= "DSCP:               $resv->{dscp}\n";

    if ($resv->{class}) {
        $msg .= "Class:              $resv->{class}\n\n";
    }
    else { $msg .= "Class:              DEFAULT\n\n"; }

    return( $msg );
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
