#===============================================================================
package OSCARS::Library::Reservation;

=head1 NAME

OSCARS::Library::Reservation - Encapsulates common reservation functionality 

=head1 SYNOPSIS

  use OSCARS::Library::Reservation;

=head1 DESCRIPTION

Functionality for SOAP methods dealing with reservation creation, 
cancellation, and viewing.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;
use vars qw(@ISA);
@ISA = qw(OSCARS::Library::TimeConverter);

use Data::Dumper;

use OSCARS::Library::TimeConverter;
use OSCARS::Library::Topology::Router;
use OSCARS::Library::Topology::Host;
use OSCARS::Library::Topology::Path;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{host} = OSCARS::Library::Topology::Host->new(
                                               'db' => $self->{db} );
    $self->{router} = OSCARS::Library::Topology::Router->new(
                                               'db' => $self->{db} );
    $self->{path} = OSCARS::Library::Topology::Path->new(
                                               'db' => $self->{db} );
    $self->{pssConfigs} = $self->getPSSConfiguration();
} #____________________________________________________________________________


###############################################################################
# createReply:  get the return fields for a createReservation request.
#
# In:  reference to hash of parameters
# Out: reference to hash of reservation details
#
sub createReply {
    my( $self, $id ) = @_;

    my( $statement, $fields );

    $statement = 'SELECT tag, status FROM ReservationUserDetails WHERE id = ?';
    $fields = $self->{db}->getRow($statement, $id);
    my $results = { 'status' => $fields->{status}, 'tag' => $fields->{tag} };
    return $results;
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
    my $results = $self->format($fields);
    return $results;
} #____________________________________________________________________________


###############################################################################
# update: change the status of the reservervation from pending to active
#
sub update {
    my( $self, $resv, $status ) = @_;

    if ( !$resv->{lspStatus} ) {
        $resv->{lspStatus} = "Successful configuration";
        $status = $self->updateStatus($resv->{tag}, $status);
    } else { $status = $self->updateStatus($resv->{tag}, 'failed'); }
    return $status;
} #____________________________________________________________________________


###############################################################################
# checkOversubscribed:  gets the list of active reservations at the same time
#   as this (proposed) reservation.  Also queries the db for the max speed of
#   the router interfaces to see if we have exceeded it.
#
# In:  hash of SOAP parameters
# Out: None
#
sub checkOversubscribed {
    my( $self, $request ) = @_;

    my( %ifaceIdxs, $row, $routerName, $addresses, $maxUtilization );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my $maxReservationUtilization = 0.50; 
    my $bandwidth = $request->{bandwidth} * 1000000;

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    my $statement = 'SELECT bandwidth, startTime, endTime, pathId ' .
          'FROM reservations WHERE endTime >= ? AND ' .
          "startTime <= ? AND (status = 'pending' OR status = 'active')";

    # do query with the comparison start & end datetime strings
    my $reservations = $self->{db}->doSelect( $statement,
           $request->{startTime}, $request->{endTime});

    # Get the bandwidth associated with each hop in the requested path
    $addresses = $request->{path}->getHops();
    for my $hop ( @{$addresses} ) {
        # get interface associated with that address
        my $link = $self->{router}->interface( $hop );
        $ifaceIdxs{$link} = $bandwidth;
    }

    # Loop through all active reservations, getting bandwidth allocated to 
    # each interface on that path.
    for my $res (@$reservations) {
        # get hops in reservation's path
        $addresses = $self->{path}->addresses( $res->{pathId} );
        for my $hop ( @{$addresses} ) {
            # get interface associated with that address
            my $link = $self->{router}->interface( $hop );
            $ifaceIdxs{$link} += $res->{bandwidth};
        }
    }
    $statement = 'SELECT name, valid, speed FROM CheckOversubscribe ' .
                 'WHERE id = ?';
    # now for each of those interface idx
    for my $idx (keys %ifaceIdxs) {
        # get max bandwith speed for an idx
        $row = $self->{db}->getRow($statement, $idx);
        if (!$row ) { next; }

        if ( $row->{valid} eq 'False' ) {
            throw Error::Simple("interface $idx not valid");
        }
 
        if ( $row->{speed} == 0 ) { next; }
        $maxUtilization = $row->{speed} * $maxReservationUtilization;
        if ($ifaceIdxs{$idx} > $maxUtilization) {
            my $errorMsg;
            # only print router name if user is authorized
            if ( $self->{user}->authorized('Reservations', 'manage') ||
                 $self->{user}->authorized('Domains', 'set' ) ) {
                $errorMsg = "$row->{name} oversubscribed: ";
            }
            else { $errorMsg = 'Route oversubscribed: '; }
            throw Error::Simple("$errorMsg  $ifaceIdxs{$idx}" .
                  " Mbps > $maxUtilization Mbps\n");
        }
    }
} #____________________________________________________________________________


###############################################################################
# insert:  convert parameters as necessary to build fields to insert
#      into reservations table, and do insert.
# In:  reference to array of fields to insert
# Out: row id.
#
sub insert {
    my( $self, $request ) = @_;

    my $fields = {};
    $fields->{id} = 'NULL';
    $fields->{startTime} =
        $self->datetimeToSeconds($request->{startTime} );
    $fields->{endTime} =
        $self->datetimeToSeconds( $request->{endTime} ) ;
    $fields->{createdTime} = time();
    $fields->{origTimeZone} = "'$request->{origTimeZone}'";
    $fields->{bandwidth} = $request->{bandwidth} * 1000000;
    $fields->{burstLimit} = $request->{burstLimit} ? 
                  $request->{burstLimit} : $self->{pssConfigs}->{burstLimit} ;
    $fields->{login} = "'$self->{user}->{login}'" ;
    $fields->{status} = "'pending'" ;
    $fields->{class} = $request->{class} ?
                  "'$request->{class}'" : "'$self->{pssConfigs}->{CoS}'" ;
    $fields->{srcPort} = $request->{srcPort} ?  $request->{srcPort} : 'NULL';
    $fields->{destPort} = $request->{destPort} ? 
                  $request->{destPort} : 'NULL';
    $fields->{dscp} = $request->{dscp} ?
                  "'$request->{dscp}'" : "'$self->{pssConfigs}->{dscp}'" ;
    $fields->{protocol} = $request->{protocol} ?
                  "'$request->{protocol}'" : 'NULL' ;

    # use requested path information to insert rows into paths and pathIpaddrs
    # tables, and get back foreign key of row in paths table
    $fields->{pathId} = $request->{path}->insert();
    $fields->{description} = "'$request->{description}'" ;
    $fields->{srcHostId} = $self->{host}->toId( $request->{srcHost} );
    $fields->{destHostId} = $self->{host}->toId( $request->{destHost} );
    my @k = keys %$fields;
    my @v = values %$fields;
    my $strKeys = join (', ', @k);
    my $strValues = join ( ', ', @v );
    my $statement = qq{INSERT INTO reservations ( $strKeys ) VALUES ( $strValues ) };
    $self->{db}->execStatement($statement);
    my $id = $self->{db}->getPrimaryId();
    return $id;
} #____________________________________________________________________________


###############################################################################
# format:  Format results to be sent back from SOAP methods, given fields from 
# reservations table. 
#
sub format {
    my( $self, $fields ) = @_;

    my $results = {};
    $results->{tag} = $fields->{tag};
    $results->{startTime} = $self->secondsToDatetime(
                              $fields->{startTime}, $fields->{origTimeZone});
    $results->{endTime} = $self->secondsToDatetime(
                              $fields->{endTime}, $fields->{origTimeZone});
    $results->{createdTime} = $self->secondsToDatetime(
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
    # Using foreign key, get all hops in associated path and convert to
    # string representation
    if ( $fields->{pathId} ) {
        $results->{path} = $self->{path}->toString( $fields->{pathId} );
    }
    $results->{description} = $fields->{description};
    $results->{srcHost} = $fields->{srcHost};
    $results->{destHost} = $fields->{destHost};
    # The following field is only set if coming in from the scheduler
    if ( $fields->{lspStatus} ) {
        $results->{lspStatus} = $fields->{lspStatus};
        my $configTime = time();
        $results->{lspConfigTime} = $self->secondsToDatetime(
                                         $configTime, $fields->{origTimeZone} );
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
#
sub summaryList {
    my( $self ) = @_;

    my( $rows, $statement );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationList ORDER BY startTime DESC';
        $rows = $self->{db}->doSelect($statement);
    }
    else {
        $statement = 'SELECT * FROM ReservationList WHERE login = ? ' .
                     'ORDER BY startTime DESC';
        $rows = $self->{db}->doSelect($statement, $self->{user}->{login});
    }
    # format results before returning
    my @results = ();
    for my $row ( @{$rows} ) {
        push( @results, $self->summarize($row) );
    }
    return \@results;
} #____________________________________________________________________________


###############################################################################
#
sub getPSSConfiguration {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM topology.configPSS where id = 1";
    my $configs = $self->{db}->getRow($statement);
    return $configs;
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


##################
# Internal methods
##################

###############################################################################
#
sub summarize {
    my( $self, $row ) = @_;

    my $results = {};
    $results->{startTime} = $self->secondsToDatetime(
                              $row->{startTime}, $row->{origTimeZone});
    $results->{endTime} = $self->secondsToDatetime(
                              $row->{endTime}, $row->{origTimeZone});
    $results->{tag} = $row->{tag};
    $results->{status} = $row->{status};
    $results->{srcHost} = $row->{srcHost};
    $results->{destHost} = $row->{destHost};
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
