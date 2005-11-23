###############################################################################
package BSS::Frontend::SOAPMethods;

# SOAP methods for BSS.
#
# Note that all authentication and authorization handling is assumed
# to have been previously done by AAAS.  Use caution if running the
# BSS on a separate machine from the one running the AAAS.
#
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use Error qw(:try);
use Data::Dumper;

use BSS::Frontend::DBRequests;
use BSS::Frontend::Policy;
use BSS::Traceroute::RouteHandler;
use BSS::Scheduler::SOAPMethods;

# until can get MySQL and views going
my $user_fields =
    'reservation_id, user_dn, ' .
    'reservation_start_time, reservation_end_time, reservation_status, ' .
    'src_hostaddr_id, dst_hostaddr_id, reservation_tag';


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{policy} = BSS::Frontend::Policy->new(
                       'dbconn' => $self->{dbconn});
    $self->{route_setup} = BSS::Traceroute::RouteHandler->new(
                                               'dbconn' => $self->{dbconn});
    $self->{scheduler} = BSS::Scheduler::SOAPMethods->new(
                                               'dbconn' => $self->{dbconn});
} #____________________________________________________________________________ 


###############################################################################
# create_reservation:  SOAP call to insert a row into the reservations table. 
#     BSS::Traceroute::RouteHandler is called to set up the route before
#     inserting a reservation in the database
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub create_reservation {
    my( $self, $params ) = @_;
    my( $duration_seconds );

    # params fields having to do with traceroute modified in find_interface_ids
    $self->{route_setup}->find_interface_ids($params);
    $self->{dbconn}->setup_times($params, $self->get_infinite_time());
    ( $params->{reservation_class}, $params->{reservation_burst_limit} ) =
                    $self->{route_setup}->get_pss_fields();

    # convert requested bandwidth to bps
    $params->{reservation_bandwidth} *= 1000000;
    $self->{policy}->check_oversubscribe($params);

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $params->{src_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($params->{source_ip}); 
    $params->{dst_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($params->{destination_ip}); 

    my $results = $self->get_results($params);
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# delete_reservation:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub delete_reservation {
    my( $self, $params ) = @_;

    my $status =  $self->{dbconn}->update_status( $params, 'precancel' );
    return $self->get_reservation_details($params);
} #____________________________________________________________________________ 


###############################################################################
# view_reservations: get all reservations from the database that satisfy a
#     particular SQL filter
#
# In: reference to hash of parameters
# Out: reservations if any, and status message
#
sub view_reservations {
    my( $self, $params ) = @_;

    my( $statement, $rows );

    # TODO:  fix authorizations, unprivileged user can access another
    #        person's reservations, etc.
    if ( $params->{engr_permission} ) {
        $statement = "SELECT *";
    }
    else {
        $statement = "SELECT $user_fields";
    }
    if ($params->{sql_filter} ne 'all') {
        my @filter_pair = split('=', $params->{sql_filter});
        $statement .= " FROM reservations WHERE $filter_pair[0] = ?" .
                      ' ORDER BY reservation_start_time';
        $rows = $self->{dbconn}->do_query($statement, $filter_pair[1]);
    }
    else {
        $statement .= ' FROM reservations' .
                      ' ORDER BY reservation_start_time';
        $rows = $self->{dbconn}->do_query($statement);
    }
    if ( $params->{engr_permission} ) { 
        $self->{dbconn}->get_engr_fields($rows); 
    }
    for my $resv ( @$rows ) {
        $self->{dbconn}->convert_times($resv);
        $self->{dbconn}->get_host_info($resv);
        $self->check_nulls($resv);
    }
    return $rows;
} #____________________________________________________________________________ 


#################
# Scheduler methods
#################

###############################################################################
# find_pending_reservations
#
sub find_pending_reservations {
    my( $self, $params ) = @_;

    return $self->{scheduler}->find_pending_reservations($params);
} #____________________________________________________________________________ 


###############################################################################
# find_expired_reservations
#
sub find_expired_reservations {
    my( $self, $params ) = @_;

    return $self->{scheduler}->find_expired_reservations($params);
} #____________________________________________________________________________ 



#################
# Private methods
#################

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $self, $dtime ) = @_;

    my @ymd = split(' ', $dtime);
    return $ymd[0];
} #____________________________________________________________________________ 


###############################################################################
# get_infinite_time:  returns "infinite" time
#
sub get_infinite_time {
    my( $self ) = @_;

    return '2039-01-01 00:00:00';
} #____________________________________________________________________________ 


###############################################################################
# get_results:  
#
sub get_results {
    my( $self, $params ) = @_;

    # build fields to insert
    my $statement = "SHOW COLUMNS from reservations";
    my $rows = $self->{dbconn}->do_query( $statement );
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
    my $unused = $self->{dbconn}->do_query($statement, @insertions);
    $results->{reservation_id} = $self->{dbconn}->get_primary_id();
    # copy over non-db fields
    $results->{source_host} = $params->{source_host};
    $results->{destination_host} = $params->{destination_host};
    # clean up NULL values
    $self->check_nulls($results);
    # convert times back to user's time zone for mail message
    $self->{dbconn}->convert_times($results);

    # set user-semi-readable tag
    $results->{reservation_tag} = $params->{user_dn} . '.' .
        $self->get_time_str($params->{reservation_start_time}) .  "-" .
        $results->{reservation_id};
    $statement = "UPDATE reservations SET reservation_tag = ?,
                 reservation_status = 'pending'
                 WHERE reservation_id = ?";
    $unused = $self->{dbconn}->do_query($statement,
                      $results->{reservation_tag}, $results->{reservation_id});
    # get loopback fields if have engr privileges
    # TODO:  need authorization for these fields
    $self->{dbconn}->get_engr_fields($results); 
    $results->{reservation_tag} =~ s/@/../;
    $results->{reservation_status} = 'pending';
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# check_nulls:  
#
sub check_nulls {
    my( $self, $resv ) = @_ ;

    my( $resv );

    # clean up NULL values
    if (!$resv->{reservation_protocol} ||
        ($resv->{reservation_protocol} eq 'NULL')) {
        $resv->{reservation_protocol} = 'DEFAULT';
    }
    if (!$resv->{reservation_dscp} ||
        ($resv->{reservation_dscp} eq 'NU')) {
        $resv->{reservation_dscp} = 'DEFAULT';
    }
} #____________________________________________________________________________ 


######
1;
# vim: et ts=4 sw=4
