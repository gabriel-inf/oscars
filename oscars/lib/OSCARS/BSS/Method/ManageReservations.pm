#==============================================================================
package OSCARS::BSS::Method::ManageReservations;

=head1 NAME

OSCARS::BSS::Method::ManageReservations - Handles OSCARS reservations. 

=head1 SYNOPSIS

  use OSCARS::BSS::Method::ManageReservations;

=head1 DESCRIPTION

SOAP method to manage reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

March 24, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::BSS::RouteHandler;
use OSCARS::BSS::ReservationCommon;
use OSCARS::BSS::TimeConversionCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{route_setup} = OSCARS::BSS::RouteHandler->new(
                                                 'user' => $self->{user});
    $self->{resv_lib} = OSCARS::BSS::ReservationCommon->new(
                                                 'user' => $self->{user});
    $self->{time_lib} = OSCARS::BSS::TimeConversionCommon->new(
                                                 'user' => $self->{user},
                                                 'logger' => $self->{logger});
} #____________________________________________________________________________


###############################################################################
# soap_method:  Handles all operations for the Manage Reservations page. 
#     It uses information from the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    if (!$self->{params}->{op}) {
        throw Error::Simple("SOAP method $self->{params}->{method} requires specifying an operation");
    }
    my $results = {};
    my $msg;
    $results->{user_login} = $self->{user}->{login};
    if ( $self->{params}->{op} eq 'createReservationForm' ) {
        return $results;
    }
    elsif ($self->{params}->{op} eq 'viewReservations') {
        ;
    }
    elsif ($self->{params}->{op} eq 'createReservation') {
        $self->create_reservation( $self->{user}, $self->{params} );
        # in this case, want to pass back current status of parameters, not
        # list of reservations
        if ( $self->{params}->{next_domain} ) {
            return $self->{params};
        }
        $msg = 'Created reservation';
        $self->{logger}->add_string("Created reservation.");
        $self->{logger}->write_file( $self->{user}->{login},
                                     $self->{params}->{method});
    }
    elsif ($self->{params}->{op} eq 'cancelReservation') {
        $self->cancel_reservation( $self->{user}, $self->{params} );
        $self->{logger}->add_string("Cancelled reservation.");
        $self->{logger}->write_file( $self->{user}->{login},
                                     $self->{params}->{method});
    }
    elsif ( ($self->{params}->{op} eq 'archiveReservations' ) &&
        ($self->{user}->authorized('Reservations', 'manage'))) {
            $self->archive_reservations( $self->{user}, $self->{params} );
    }
    $results->{list} = $self->get_reservations($self->{user}, $self->{params});
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_reservations:  get reservations from the database.  If the user has
#     the 'manage' permission on the 'Reservations' resource, they can view 
#     all reservations.  Otherwise they can only view their own.
#
# In:  reference to hash of parameters
# Out: reference to array of hashes
#
sub get_reservations {
    my( $self, $user, $params ) = @_;

    my( $rows, $statement );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = "SELECT * FROM BSS.reservations" .
                     ' ORDER BY reservation_start_time';
        $rows = $user->do_query($statement);
    }
    else {
        $statement = 'SELECT * FROM BSS.reservations' .
                     ' WHERE user_login = ?' .
                     ' ORDER BY reservation_start_time';
        $rows = $user->do_query($statement, $user->{login});
    }
    for my $resv ( @$rows ) {
        $self->{time_lib}->convert_times($resv);
        $self->{resv_lib}->get_host_info($resv);
    }
    $self->{logger}->add_string("Reservations list");
    $self->{logger}->write_file($user->{login}, $params->{method});
    return $rows;
} #____________________________________________________________________________


###############################################################################
# create_reservation:  inserts a row into the reservations table. 
#     OSCARS::BSS::RouteHandler is called to set up the route before
#     inserting a reservation in the database
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub create_reservation {
    my( $self, $user, $params ) = @_;
    my( $duration_seconds );

    # params fields having to do with traceroute modified in find_interface_ids
    $self->{route_setup}->find_interface_ids($self->{logger}, $params);
    # if next_domain is set, forward to OSCARS/BRUW server in next domain
    if ($params->{next_domain} ) {
        return $params;
    }
    ( $params->{reservation_start_time}, $params->{reservation_end_time},
      $params->{reservation_created_time} ) =
          $self->{time_lib}->setup_times( $params->{reservation_start_time},
                                          $params->{duration_hour});

    my $pss_configs = $self->{resv_lib}->get_pss_configs();
    $params->{reservation_class} = $pss_configs->{pss_conf_CoS};
    $params->{reservation_burst_limit} = $pss_configs->{pss_conf_burst_limit};

    # convert requested bandwidth to bps
    $params->{reservation_bandwidth} *= 1000000;
    $self->check_oversubscribe($user, $params);

    # Get hosts table id from source's and destination's host name or ip
    # address.
    $params->{src_host_id} =
        $self->{resv_lib}->host_ip_to_id($params->{source_ip}); 
    $params->{dst_host_id} =
        $self->{resv_lib}->host_ip_to_id($params->{destination_ip}); 

    my $results = $self->build_results($user, $params);
    $self->{logger}->add_hash($results);
    $self->{logger}->write_file($user->{login}, $params->{method});
    return $results;
} #____________________________________________________________________________


###############################################################################
# cancel_reservation:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub cancel_reservation {
    my( $self, $user, $params ) = @_;

    # TODO:  ensure unprivileged user can't cancel another's reservation
    my $status =  $self->{resv_lib}->update_status( $params->{reservation_id},
                                                    'precancel' );
    my $results = $self->{resv_lib}->view_details($params);
    $results->{reservation_id} = $params->{reservation_id};
    $self->{time_lib}->convert_times($results);
    $self->{logger}->add_hash($results);
    $self->{logger}->write_file($user->{login}, $params->{method});
    return $results;
} #____________________________________________________________________________


###############################################################################
# archive_reservations:  called periodically by the scheduler to archive 
# reservations.
#
# In:  reference to hash of parameters
# Out: reference to array of hashes
#
sub archive_reservations {
    my( $self, $user, $params ) = @_;

    my $statement = 'SELECT * FROM BSS.reservations  ' .
                 "WHERE reservation_status = 'finished' " .
                 "OR reservation_status = 'cancelled' " .
                 "OR reservation_status = 'failed' " .
                 'AND reservation_start_time < ? ';
    my $rows = $user->do_query($statement, $params->{archive_time});
    $self->{resv_methods}->get_engr_fields($rows->[0]); 
    for my $resv ( @$rows ) {
        $self->{resv_methods}->get_host_info($resv);
        $self->{resv_methods}->check_nulls($resv);
        $self->{logger}->add_hash($resv);
    }
    $self->{logger}->write_file($user->{login}, $params->{method});
    return $rows;
} #____________________________________________________________________________


#######################
# Private methods.
#######################

###############################################################################
# reservation_created_messages:  generate email message
#
sub reservation_created_messages {
    my( $self, $resv ) = @_;

    my( @messages );
    my $user_login = $self->{user}->{login};
    my $msg = "Reservation scheduled by $user_login with parameters:\n";
    $msg .= $self->{resv_lib}->reservation_stats($resv);
    my $subject_line = "Reservation scheduled by $user_login.";
    push(@messages, { 'msg' => $msg, 'subject_line' => $subject_line, 'user' => $user_login } ); 
    return( \@messages );
} #____________________________________________________________________________


###############################################################################
# id_to_router_name:  get the router name given the interface primary key.
# In:  interface table key id
# Out: router name
#
sub id_to_router_name {
    my( $self, $interface_id ) = @_;

    my $statement = 'SELECT router_name FROM BSS.routers
                 WHERE router_id = (SELECT router_id from BSS.interfaces
                                    WHERE interface_id = ?)';
    my $row = $self->{user}->get_row($statement, $interface_id);
    # no match
    if ( !$row ) {
        # not considered an error
        return '';
    }
    return $row->{router_name};
} #____________________________________________________________________________


###############################################################################
# reservation_cancelled_messages:  generate cancelled email message
#
sub reservation_cancelled_messages {
    my( $self, $resv ) = @_;

    my( @messages );
    my $user_login = $self->{user}->{login};
    my $msg = "Reservation cancelled by $user_login with parameters:\n";
    $msg .= $self->{resv_lib}->reservation_stats($resv);
    my $subject_line = "Reservation cancelled by $user_login.";
    push(@messages, { 'msg' => $msg, 'subject_line' => $subject_line, 'user' => $user_login } ); 
    return( \@messages );
} #____________________________________________________________________________


###############################################################################
# check_overscribe:  gets the list of active reservations at the same time as
#   this (proposed) reservation.  Also queries the db for the max speed of the
#   router interfaces to see if we have exceeded it.
#
# In: list of reservations, new reservation
# OUT: valid (0 or 1), and error message
#
sub check_oversubscribe {
    my( $self, $user, $params ) = @_;

    my( %iface_idxs, $row, $reservation_path, $link, $res, $idx );
    my( $router_name );
    # maximum utilization for a particular link
    my( $max_utilization );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my( $max_reservation_utilization ) = 0.50; 

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    my $statement = 'SELECT reservation_bandwidth, reservation_start_time,
              reservation_end_time, reservation_path FROM BSS.reservations
              WHERE reservation_end_time >= ? AND
                  reservation_start_time <= ? AND ' .
                  " (reservation_status = 'pending' OR
                   reservation_status = 'active')";

    # handled query with the comparison start & end datetime strings
    my $reservations = $user->do_query( $statement,
           $params->{reservation_start_time}, $params->{reservation_end_time});

    # assign the new path bandwidths 
    for $link (@{$params->{reservation_path}}) {
        $iface_idxs{$link} = $params->{reservation_bandwidth};
    }

    # loop through all active reservations
    for $res (@$reservations) {
        # get bandwidth allocated to each idx on that path
        for $reservation_path ( $res->{reservation_path} ) {
            for $link ( split(' ', $reservation_path) ) {
                $iface_idxs{$link} += $res->{reservation_bandwidth};
            }
        }
    }
    # now for each of those interface idx
    for $idx (keys %iface_idxs) {
        # get max bandwith speed for an idx
        $row = $self->get_interface_fields($idx);
        if (!$row ) { next; }

        if ( $row->{interface_valid} eq 'False' ) {
            throw Error::Simple("interface $idx not valid");
        }
 
        $max_utilization = $row->{interface_speed} *
                           $max_reservation_utilization;
        if ($iface_idxs{$idx} > $max_utilization) {
            my $error_msg;
            # only print router name if user has admin privileges
            if ($params->{form_type} eq 'admin') {
                $router_name = $self->id_to_router_name( $idx );
                $error_msg = "$router_name oversubscribed: ";
            }
            else { $error_msg = 'Route oversubscribed: '; }
            throw Error::Simple("$error_msg  $iface_idxs{$idx}" .
                  " Mbps > $max_utilization Mbps\n");
        }
    }
    # Replace array @$params->{reservation_path} with string separated by
    # spaces
    $params->{reservation_path} = join(' ', @{$params->{reservation_path}});
} #____________________________________________________________________________


###############################################################################
# get_interface_fields:  get the bandwidth of a router interface.
#
# IN: router interface idx
# OUT: interface row
#
sub get_interface_fields {
    my( $self, $iface_id) = @_;

    my $statement = 'SELECT * FROM BSS.interfaces WHERE interface_id = ?';
    my $row = $self->{user}->get_row($statement, $iface_id);
    return $row;
} #____________________________________________________________________________


###############################################################################
# build_results:  build fields to insert in reservations row
#
sub build_results  {
    my( $self, $user, $params ) = @_;

    my $statement = 'SHOW COLUMNS from BSS.reservations';
    my $rows = $user->do_query( $statement );
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
    $statement = "INSERT INTO BSS.reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    my $unused = $user->do_query($statement, @insertions);
    $results->{reservation_id} = $user->get_primary_id();
    # copy over non-db fields
    $results->{source_host} = $params->{source_host};
    $results->{destination_host} = $params->{destination_host};
    # clean up NULL values
    $self->{resv_lib}->check_nulls($results);
    # convert times back to user's time zone for mail message
    $self->{time_lib}->convert_times($results);

    # set user-semi-readable tag
    $results->{reservation_tag} = $user->{login} . '.' .
        $self->{time_lib}->get_time_str(
              $params->{reservation_start_time}) .  "-" .
              $results->{reservation_id};
    $statement = "UPDATE BSS.reservations SET reservation_tag = ?,
                 reservation_status = 'pending'
                 WHERE reservation_id = ?";
    $unused = $user->do_query($statement,
                      $results->{reservation_tag}, $results->{reservation_id});
    # Get loopback fields if authorized.
    if ( $self->{user}->authorized('Reservations', 'manage') ||
         $self->{user}->authorized('Domains', 'set' ) ) {
        $self->{resv_lib}->get_engr_fields($results); 
    }
    $results->{reservation_tag} =~ s/@/../;
    $results->{reservation_status} = 'pending';
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
