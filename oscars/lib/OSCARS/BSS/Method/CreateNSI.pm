#==============================================================================
package OSCARS::BSS::Method::CreateNSI;

=head1 NAME

OSCARS::BSS::Method::CreateNSI - Handles creation of circuit reservation. 

=head1 SYNOPSIS

  use OSCARS::BSS::Method::CreateNSI;

=head1 DESCRIPTION

SOAP method to create reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

April 11, 2006

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
# soap_method:  Handles reservation creation. 
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $results = $self->create_reservation( $self->{user}, $self->{params} );
    $results->{user_login} = $self->{user}->{login};
    if ( $results->{next_domain} ) {
        $self->{logger}->info("forwarding",
                         { 'next_domain' => $results->{next_domain} });
    }
    return $results;
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

    $self->{logger}->info("start", $self->{params});
    # params fields having to do with traceroute modified in find_interface_ids
    my $results =
        $self->{route_setup}->find_interface_ids($self->{logger}, $params);
    # if next_domain is set, forward to OSCARS/BRUW server in next domain
    if ($results->{next_domain} ) {
        return $self->next_domain_parameters($params, $results->{next_domain});
    }
    $params->{ingress_interface_id} = $results->{ingress_interface_id};
    $params->{ingress_ip} = $results->{ingress_ip};
    $params->{egress_interface_id} = $results->{egress_interface_id};
    $params->{egress_ip} = $results->{egress_ip};
    $params->{reservation_path} = $results->{reservation_path};

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
        $self->{resv_lib}->host_ip_to_id($results->{source_ip}); 
    $params->{dst_host_id} =
        $self->{resv_lib}->host_ip_to_id($results->{destination_ip}); 

    $results = $self->build_results($user, $params);
    $self->{logger}->info("finish", $results);
    return $results;
} #____________________________________________________________________________


###############################################################################
# generate_message:  generate email message
#
sub generate_message {
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
# next_domain_parameters:  modify parameters before sending to next domain
#
sub next_domain_parameters {
    my( $self, $params, $next_domain ) = @_;

    my $results = {};
    for my $idx (keys %{$params}) {
        $results->{$idx} = $params->{$idx};
    }
    $results->{next_domain} = $next_domain;
    $results->{ingress_router} = undef;
    $results->{egress_router} = undef;
    return $results;
} #____________________________________________________________________________


###############################################################################
# build_results:  build fields to insert in reservations row
#
sub build_results {
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

    my @ymd = split(' ', $params->{reservation_start_time});
    # set user-semi-readable tag
    # FIX:  more domain independence
    $results->{reservation_tag} = 'ESNet' . '-' . $user->{login} . '.' .
          $ymd[0] .  "-" .  $results->{reservation_id};
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
