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

May 1, 2006

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
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $pathInfo;

    $self->{logger}->info("start", $self->{params});
    # find path, and see if the next domain needs to be contacted
    ( $pathInfo, $self->{pssConfigs} ) = $self->{pathfinder}->findPath(
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
    my $results = $self->createReservation( $self->{params}, $pathInfo);
    $self->{logger}->info("finish", $results);
    return $results;
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
    my( $self, $params, $pathInfo ) = @_;
    my( $durationSeconds );

    $self->checkOversubscribed($params);
    my( $fields, $startTime, $createdTime ) = 
        $self->buildFields($params, $pathInfo);
    my $statement = "INSERT INTO reservations VALUES(" .
                     join(', ', @$fields) . ")";
    $self->{db}->execStatement($statement);
    my $id = $self->{db}->getPrimaryId();
    return $self->buildResults($id, $params, $startTime, $createdTime,
                               $pathInfo);
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
    my( $self, $params ) = @_;

    my( %ifaceIdxs, $row, $routerName, $maxUtilization );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my $maxReservationUtilization = 0.50; 
    my $bandwidth = $params->{bandwidth} * 1000000;

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    my $statement = 'SELECT bandwidth, startTime, endTime, path ' .
          'FROM reservations WHERE endTime >= ? AND ' .
          "startTime <= ? AND (status = 'pending' OR status = 'active')";

    # do query with the comparison start & end datetime strings
    my $reservations = $self->{db}->doSelect( $statement,
           $params->{startTime}, $params->{endTime});

    # assign the new path bandwidths 
    for my $link (@{$params->{path}}) {
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
    my( $self, $params, $pathInfo ) = @_;

    my @fields = ();

    push(@fields, 'NULL');   # id
    my $startTime = $self->{timeLib}->datetimeToSeconds($params->{startTime} );
    push(@fields, $startTime);
    push(@fields, $self->{timeLib}->datetimeToSeconds( $params->{endTime} ) );
    my $createdTime = time();
    push(@fields, $createdTime );
    push(@fields, "'$params->{origTimeZone}'" );
    push(@fields, $params->{bandwidth} * 1000000 );
    push(@fields, $params->{burstLimit} ? 
                  $params->{burstLimit} : $self->{pssConfigs}->{burstLimit} );
    push(@fields, "'$self->{user}->{login}'" );
    push(@fields, "'pending'" );   # status
    push(@fields, $params->{class} ?
                  "'$params->{class}'" : "'$self->{pssConfigs}->{CoS}'" );
    push(@fields, $params->{srcPort} ?  $params->{srcPort} : 'NULL' );
    push(@fields, $params->{destPort} ? $params->{destPort} : 'NULL' );
    push(@fields, $params->{dscp} ? "'$params->{dscp}'" : "'$self->{pssConfigs}->{dscp}'" );
    push(@fields, $params->{protocol} ? "'$params->{protocol}'" : 'NULL' );
    my $pathStr = join(' ', @{$pathInfo->{path}});
    push(@fields, "'$pathStr'" );
    push(@fields, "'$params->{description}'" );
    push(@fields, $pathInfo->{ingressInterfaceId} );
    push(@fields, $pathInfo->{egressInterfaceId} );
    my $srcHostId = $self->{resvLib}->hostIPToId($pathInfo->{srcIP});
    my $destHostId = $self->{resvLib}->hostIPToId($pathInfo->{destIP});
    push(@fields, $srcHostId );
    push(@fields, $destHostId );
    return ( \@fields, $startTime, $createdTime );
} #____________________________________________________________________________


###############################################################################
# buildResults:  build results hash
#
sub buildResults {
    my( $self, $id, $params, $startTime, $createdTime, $pathInfo ) = @_;

    my $results = {};
    # TODO:  don't send back fields that are a straight copy from params
    $results->{startTime} = $params->{startTime};
    $results->{endTime} = $params->{endTime};
    $results->{createdTime} = $self->{timeLib}->secondsToDatetime(
                              $createdTime, $params->{origTimeZone});
    $results->{origTimeZone} = $params->{origTimeZone};
    $results->{bandwidth} = $params->{bandwidth} * 1000000;
    $results->{burstLimit} = $params->{burstLimit} ? 
                     $params->{burstLimit} : $self->{pssConfigs}->{burstLimit};
    $results->{login} = $self->{user}->{login};
    $results->{status} = 'pending';
    $results->{class} = $params->{class} ?
                     $params->{class} : $self->{pssConfigs}->{CoS};
    $results->{dscp} = $params->{dscp};
    $results->{srcPort} = $params->{srcPort} ?  $params->{srcPort} : 'DEFAULT';
    $results->{destPort} = $params->{destPort} ?  $params->{destPort} : 'DEFAULT';
    $results->{protocol} = $params->{protocol};
    $results->{srcHost} = $params->{destHost};
    $results->{destHost} = $params->{destHost};
    $results->{description} = $params->{description};
    # clean up NULL values
    $self->{resvLib}->checkNulls($results);

    # Get loopback fields and path if authorized.
    if ( $self->{user}->authorized('Reservations', 'manage') ||
         $self->{user}->authorized('Domains', 'set' ) ) {
        ( $results->{ingressRouterIP}, $results->{ingressLoopbackIP} ) =
               $self->{resvLib}->getRouterInfo($pathInfo->{ingressInterfaceId}); 
        ( $results->{egressRouterIP}, $results->{egressLoopbackIP} ) =
               $self->{resvLib}->getRouterInfo($pathInfo->{egressInterfaceId}); 
        my $p = join(' ', @{$pathInfo->{path}});
        my $pathArray = $self->{resvLib}->getPathRouterInfo($p);
        $results->{path} = join(' ', @{$pathArray});
    }
    # get YYYY-MM-DD string to insert into reservation tag 
    my $ymd = $self->{timeLib}->getYMD($startTime);
    # Subsequent retrieval of information from db uses a view to compute the 
    # tag.
    my $statement = "SELECT abbrev FROM domains WHERE local=1";
    my $row = $self->{db}->getRow($statement);
    $results->{tag} = $row->{abbrev} . '-' . $self->{user}->{login} . '.' .
          $ymd .  "-" .  $id;
    $results->{tag} =~ s/@/../;
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
