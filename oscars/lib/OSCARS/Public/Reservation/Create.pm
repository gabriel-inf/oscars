#==============================================================================
package OSCARS::Public::Reservation::Create;

=head1 NAME

OSCARS::Public::Reservation::Create - Handles creation of circuit reservation. 

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Create;

=head1 DESCRIPTION

SOAP method to create reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

May 4, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Topology::Pathfinder;
use OSCARS::Library::Reservation::Common;
use OSCARS::Library::Reservation::TimeConversion;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{pathfinder} = OSCARS::Library::Topology::Pathfinder->new(
                             'db' => $self->{db});
    $self->{resvLib} = OSCARS::Library::Reservation::Common->new(
                             'user' => $self->{user}, 'db' => $self->{db});
    $self->{timeLib} = OSCARS::Library::Reservation::TimeConversion->new();
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles reservation creation. 
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $pathInfo;

    $logger->info("start", $request);
    # find path, and see if the next domain needs to be contacted
    ( $pathInfo, $self->{pssConfigs} ) = $self->{pathfinder}->findPath(
                                             $logger, $request );
    # if nextDomain is set, forward to corresponding method in next domain
    if ($pathInfo->{nextDomain} ) {
        $request->{pathInfo} = $pathInfo;
        # TODO:  better way of utilizing pathInfo in next domain
        # better handling of exit router, passing back next domain's tag
        $request->{nextDomain} = $pathInfo->{nextDomain};
        if ( $pathInfo->{egressRouter} ) {
            $request->{egressRouter} = $pathInfo->{egressRouter};
        }
        $logger->info("forwarding.start", $request );
        # "database" parameter is database name
        my( $errMsg, $nextPathInfo ) =
               $self->{forwarder}->forward($request, $self->{database});
        $logger->info("forwarding.finish", $nextPathInfo );
        # TODO: process any differences
    }

    if ($errMsg)
    {
        throw Error::Simple("Error forwrding reservervation to next domain: $errMsg");
    }

    # having found path, attempt to enter reservation in db
    my $response = $self->createReservation( $request, $pathInfo);
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


### Private methods. ###
 
###############################################################################
# createReservation:  builds row to insert into the reservations table,
#      checks for oversubscribed route, inserts the reservation, and
#      builds up the results to return to the client.
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub createReservation {
    my( $self, $request, $pathInfo ) = @_;
    my( $durationSeconds );

    $self->checkOversubscribed($request);
    my $fields = $self->buildFields($request, $pathInfo);
    my @k = keys %$fields;
    my @v = values %$fields;
    my $strKeys = join (', ', @k);
    my $strValues = join ( ', ', @v );
    my $statement = qq{INSERT INTO reservations ( $strKeys ) VALUES ( $strValues ) };
    $self->{db}->execStatement($statement);
    my $id = $self->{db}->getPrimaryId();
    # get results back from database (minus extra quotes)
    my $results = $self->{resvLib}->details($id);
    return $results;
} #____________________________________________________________________________


###############################################################################
# checkOverscribed:  gets the list of active reservations at the same time as
#   this (proposed) reservation.  Also queries the db for the max speed of the
#   router interfaces to see if we have exceeded it.
#
# In:  hash of SOAP parameters
# Out: None
#
sub checkOversubscribed {
    my( $self, $request ) = @_;

    my( %ifaceIdxs, $row, $routerName, $maxUtilization );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my $maxReservationUtilization = 0.50; 
    my $bandwidth = $request->{bandwidth} * 1000000;

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    my $statement = 'SELECT bandwidth, startTime, endTime, path ' .
          'FROM reservations WHERE endTime >= ? AND ' .
          "startTime <= ? AND (status = 'pending' OR status = 'active')";

    # do query with the comparison start & end datetime strings
    my $reservations = $self->{db}->doSelect( $statement,
           $request->{startTime}, $request->{endTime});

    # assign the new path bandwidths 
    for my $link (@{$request->{path}}) {
        $ifaceIdxs{$link} = $bandwidth;
    }

    # loop through all active reservations
    for my $res (@$reservations) {
        # get bandwidth allocated to each idx on that path
        for my $path ( $res->{path} ) {
            for my $link ( split(' ', $path) ) {
                $ifaceIdxs{$link} += $res->{bandwidth};
            }
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
# buildFields:  convert parameters as necessary to build fields to insert
#      into reservations table.
# In:  reference to array of fields to insert
# Out: ref to fields hash.
#
sub buildFields {
    my( $self, $request, $pathInfo ) = @_;

    my $fields = {};

    $fields->{id} = 'NULL';
    $fields->{startTime} =
        $self->{timeLib}->datetimeToSeconds($request->{startTime} );
    $fields->{endTime} =
        $self->{timeLib}->datetimeToSeconds( $request->{endTime} ) ;
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
    my $pathStr = join(' ', @{$pathInfo->{path}});
    $fields->{path} = "'$pathStr'" ;
    $fields->{description} = "'$request->{description}'" ;
    $fields->{ingressInterfaceId} = $pathInfo->{ingressInterfaceId} ;
    $fields->{egressInterfaceId} = $pathInfo->{egressInterfaceId} ;
    my $srcHostId = $self->{resvLib}->hostIPToId($pathInfo->{srcIP});
    $fields->{srcHostId} = $srcHostId;
    my $destHostId = $self->{resvLib}->hostIPToId($pathInfo->{destIP});
    $fields->{destHostId} = $destHostId;
    return $fields;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
