#==============================================================================
package OSCARS::Intradomain::ReservationCommon;

=head1 NAME

OSCARS::Intradomain::ReservationCommon - Common functionality for OSCARS reservation 
methods.

=head1 SYNOPSIS

  use OSCARS::Intradomain::ReservationCommon;

=head1 DESCRIPTION

Common functionality for SOAP methods dealing with reservation creation, 
cancellation, and viewing.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 18, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
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

    my( $statement, $row );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM reservations WHERE id = ?';
        $row = $self->{db}->getRow($statement, $params->{id});
        $self->getEngrFields($row); 
    }
    else {
        $statement = 'SELECT startTime, endTime, createdTime, ' .
            'bandwidth, burstLimit, status, class, srcPort, destPort, dscp, ' .
            'protocol, tag, description, srcHostId, destHostId, origTimeZone ' .
            'FROM reservations WHERE login = ? AND id = ?';
        $row = $self->{db}->getRow($statement, $self->{user}->{login},
                                      $params->{id});
    }
    if (!$row) { return $row; }
    
    $self->getHostInfo($row);
    $self->checkNulls($row);
    return $row;
} #____________________________________________________________________________


###############################################################################
# updateReservation: change the status of the reservervation from pending to
#                     active
#
sub updateReservation {
    my ($self, $resv, $status, $logger) = @_;

    if ( !$resv->{lspStatus} ) {
        $resv->{lspStatus} = "Successful configuration";
        $status = $self->updateStatus($resv->{id}, $status);
    } else { $status = $self->updateStatus($resv->{id}, 'failed'); }
    $logger->info('updateReservation',
        { 'status' => $status, 'id' => $resv->{id} });
} #____________________________________________________________________________


###############################################################################
# updateStatus: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub updateStatus {
    my ( $self, $id, $status ) = @_;

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
    my $unused = $self->{db}->doQuery($statement, $status, $id);
    return $status;
} #____________________________________________________________________________


###############################################################################
#
sub getPssConfigs {
    my( $self ) = @_;

        # use defaults for now
    my $statement = 'SELECT * FROM pssConfs where id = 1';
    my $configs = $self->{db}->getRow($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub getHostInfo {
    my( $self, $resv ) = @_;
 
    my $statement = 'SELECT IP FROM hosts WHERE id = ?';
    my $hrow = $self->{db}->getRow($statement, $resv->{srcHostId});
    my $ip = $hrow->{IP};
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)+';
    if ($ip =~ $regexp) { $ip = $1; }
    my $ipaddr = inet_aton($ip);
    $resv->{srcHost} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{srcHost}) {
        $resv->{srcHost} = $hrow->{IP};
    }
    $resv->{srcIP} = $hrow->{IP};

    $hrow = $self->{db}->getRow($statement, $resv->{destHostId});
    # TODO:  FIX, hrow might be empty
    $ip = $hrow->{IP};
    if ($ip =~ $regexp) { $ip = $1; }
    $ipaddr = inet_aton($ip);
    $resv->{destHost} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{destHost}) {
        $resv->{destHost} = $hrow->{IP};
    }
    $resv->{destIP} = $hrow->{IP};
} #____________________________________________________________________________


###############################################################################
#
sub getEngrFields {
    my( $self, $resv ) = @_;
 
    my $statement =
        qq{SELECT name, loopback FROM routers WHERE id =
           (SELECT routerId FROM interfaces WHERE interfaces.id = ?)};

    # TODO:  FIX row might be empty
    my $row = $self->{db}->getRow($statement, $resv->{ingressInterfaceId});
    $resv->{ingressRouter} = $row->{name}; 
    $resv->{ingressIP} = $row->{loopback}; 

    $row = $self->{db}->getRow($statement, $resv->{egressInterfaceId});
    $resv->{egressRouter} = $row->{name}; 
    $resv->{egressIP} = $row->{loopback}; 
    my @pathRouters = split(' ', $resv->{path});
    $resv->{path} = ();
    for $_ (@pathRouters) {
        $row = $self->{db}->getRow($statement, $_);
        push(@{$resv->{path}}, $row->{name}); 
    }
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
        $statement = "INSERT INTO hosts VALUES ( NULL, '$ipaddr'  )";
        my $unused = $self->{db}->doQuery($statement);
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
    if ($resv->{id}) { $msg .= "Reservation id:     $resv->{id}\n"; }

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
