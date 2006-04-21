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

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation::Pathfinder;
use OSCARS::Library::Reservation::Common;
use OSCARS::Library::Reservation::TimeConversion;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{pathfinder} = OSCARS::Library::Reservation::Pathfinder->new(
                             'db' => $self->{db});
    $self->{resvLib} = OSCARS::Library::Reservation::Common->new(
                             'user' => $self->{user}, 'db' => $self->{db});
    $self->{timeLib} = OSCARS::Library::Reservation::TimeConversion->new(
                             'db' => $self->{db}, 'logger' => $self->{logger});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles reservation creation. 
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    $self->{logger}->info("start", $self->{params});
    # find path, and see if the next domain needs to be contacted
    my $pathInfo = $self->{pathfinder}->findPath(
                                            $self->{logger}, $self->{params} );
    # if nextDomain is set, forward to corresponding method in next domain
    if ($pathInfo->{nextDomain} ) {
        $self->{params}->{pathInfo} = $pathInfo;
        # TODO:  better way of utilizing pathInfo in next domain
        # better handling of exit router, passing back next domain's tag
        $self->{params}->{nextDomain} = $pathInfo->{nextDomain};
        if ( $pathInfo->{egressRouter} ) {
            $self->{params}->{egressRouter} = $pathInfo->{egressRouter};
        }
        $self->{logger}->info("forwarding.start", $self->{params} );
        # "database" parameter is database name
        my( $errMsg, $nextPathInfo ) =
               $self->{forwarder}->forward($self->{params}, $self->{database});
        $self->{logger}->info("forwarding.finish", $nextPathInfo );
        # TODO: process any differences
    }
    # having found path, attempt to enter reservation in db
    my $results = $self->createReservation( $self->{params}, $pathInfo );
    $results->{login} = $self->{user}->{login};
    $self->{logger}->info("finish", $results);
    return $results;
} #____________________________________________________________________________


###############################################################################
# createReservation:  inserts a row into the reservations table, possibly
#      after several domains have been contacted. 
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub createReservation {
    my( $self, $params, $pathInfo ) = @_;
    my( $durationSeconds );

    $params->{ingressInterfaceId} = $pathInfo->{ingressInterfaceId};
    $params->{ingressIP} = $pathInfo->{ingressIP};
    $params->{egressInterfaceId} = $pathInfo->{egressInterfaceId};
    $params->{egressIP} = $pathInfo->{egressIP};
    $params->{path} = $pathInfo->{path};

    ( $params->{startTime}, $params->{endTime},
      $params->{createdTime} ) =
          $self->{timeLib}->setupTimes( $params->{startTime},
                                        $params->{durationHour});

    my $pssConfigs = $self->{resvLib}->getPssConfigs();
    $params->{class} = $pssConfigs->{CoS};
    $params->{burstLimit} = $pssConfigs->{burstLimit};

    # convert requested bandwidth to bps
    $params->{bandwidth} *= 1000000;
    $self->checkOversubscribed($params);

    # Get hosts table id from source's and destination's host name or ip
    # address.
    $params->{srcHostId} =
        $self->{resvLib}->hostIPToId($pathInfo->{srcIP}); 
    $params->{destHostId} =
        $self->{resvLib}->hostIPToId($pathInfo->{destIP}); 

    return $self->buildResults($params);
} #____________________________________________________________________________


###############################################################################
# generateMessage:  generate email message
#
sub generateMessage {
    my( $self, $resv ) = @_;

    my( @messages );
    my $login = $self->{user}->{login};
    my $msg = "Reservation scheduled by $login with parameters:\n";
    $msg .= $self->{resvLib}->reservationStats($resv);
    my $subject = "Reservation scheduled by $login.";
    push(@messages, { 'msg' => $msg, 'subject' => $subject, 'user' => $login } ); 
    return( \@messages );
} #____________________________________________________________________________


###############################################################################
# idToRouterName:  get the router name given the interface primary key.
# In:  interface table key id
# Out: router name
#
sub idToRouterName {
    my( $self, $interfaceId ) = @_;

    my $statement = 'SELECT name FROM routers WHERE id = 
        (SELECT routerId from interfaces WHERE interfaces.id = ?)';
    my $row = $self->{db}->getRow($statement, $interfaceId);
    # no match
    if ( !$row ) {
        # not considered an error
        return '';
    }
    return $row->{name};
} #____________________________________________________________________________


###############################################################################
# checkOverscribed:  gets the list of active reservations at the same time as
#   this (proposed) reservation.  Also queries the db for the max speed of the
#   router interfaces to see if we have exceeded it.
#
# In: list of reservations, new reservation
# OUT: valid (0 or 1), and error message
#
sub checkOversubscribed {
    my( $self, $params ) = @_;

    my( %ifaceIdxs, $row, $path, $link, $res, $idx );
    my( $routerName );
    # maximum utilization for a particular link
    my( $maxUtilization );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my $maxReservationUtilization = 0.50; 

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    my $statement = 'SELECT bandwidth, startTime, endTime, path ' .
          'FROM reservations WHERE endTime >= ? AND ' .
          "startTime <= ? AND (status = 'pending' OR status = 'active')";

    # handled query with the comparison start & end datetime strings
    my $reservations = $self->{db}->doQuery( $statement,
           $params->{startTime}, $params->{endTime});

    # assign the new path bandwidths 
    for $link (@{$params->{path}}) {
        $ifaceIdxs{$link} = $params->{bandwidth};
    }

    # loop through all active reservations
    for $res (@$reservations) {
        # get bandwidth allocated to each idx on that path
        for $path ( $res->{path} ) {
            for $link ( split(' ', $path) ) {
                $ifaceIdxs{$link} += $res->{bandwidth};
            }
        }
    }
    # now for each of those interface idx
    for $idx (keys %ifaceIdxs) {
        # get max bandwith speed for an idx
        $row = $self->getInterfaceFields($idx);
        if (!$row ) { next; }

        if ( $row->{valid} eq 'False' ) {
            throw Error::Simple("interface $idx not valid");
        }
 
        $maxUtilization = $row->{speed} * $maxReservationUtilization;
        if ($ifaceIdxs{$idx} > $maxUtilization) {
            my $errorMsg;
            # only print router name if user has admin privileges
            if ($params->{formType} eq 'admin') {
                $routerName = $self->idToRouterName( $idx );
                $errorMsg = "$routerName oversubscribed: ";
            }
            else { $errorMsg = 'Route oversubscribed: '; }
            throw Error::Simple("$errorMsg  $ifaceIdxs{$idx}" .
                  " Mbps > $maxUtilization Mbps\n");
        }
    }
    # Replace array @$params->{path} with string separated by
    # spaces
    $params->{path} = join(' ', @{$params->{path}});
} #____________________________________________________________________________


###############################################################################
# getInterfaceFields:  get the bandwidth of a router interface.
#
# IN: router interface idx
# OUT: interface row
#
sub getInterfaceFields {
    my( $self, $interfaceId) = @_;

    my $statement = 'SELECT * FROM interfaces WHERE id = ?';
    my $row = $self->{db}->getRow($statement, $interfaceId);
    return $row;
} #____________________________________________________________________________


###############################################################################
# buildResults:  build fields to insert in reservations row
#
sub buildResults {
    my( $self, $params ) = @_;

    my $statement = 'SHOW COLUMNS from reservations';
    my $rows = $self->{db}->doQuery( $statement );
    my @insertions;
    my $results = {}; 
    # TODO:  necessary to do insertions this way?
    for $_ ( @$rows ) {
       if ($params->{$_->{Field}}) {
           $results->{$_->{Field}} = $params->{$_->{Field}};
           push(@insertions, $params->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    # insert all fields for reservation into database
    $statement = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{db}->doQuery($statement, @insertions);
    $results->{id} = $self->{db}->getPrimaryId();
    # copy over non-db fields
    $results->{srcHost} = $params->{srcHost};
    $results->{destHost} = $params->{destHost};
    # clean up NULL values
    $self->{resvLib}->checkNulls($results);
    # convert times back to user's time zone for mail message
    $self->{timeLib}->convertTimes($results);

    my @ymd = split(' ', $params->{startTime});
    # set user-semi-readable tag
    # FIX:  more domain independence
    $results->{tag} = 'ESNet' . '-' . $self->{user}->{login} . '.' .
          $ymd[0] .  "-" .  $results->{id};
    $statement = 'UPDATE reservations SET tag = ?, ' .
                 "status = 'pending' WHERE id = ?";
    $unused = $self->{db}->doQuery($statement, $results->{tag},
                                    $results->{id});
    # Get loopback fields if authorized.
    if ( $self->{user}->authorized('Reservations', 'manage') ||
         $self->{user}->authorized('Domains', 'set' ) ) {
        $self->{resvLib}->getEngrFields($results); 
    }
    $results->{tag} =~ s/@/../;
    $results->{status} = 'pending';
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
