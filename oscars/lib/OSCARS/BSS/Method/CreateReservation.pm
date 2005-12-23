###############################################################################
package OSCARS::BSS::Method::CreateReservation;

=head1 NAME

OSCARS::BSS::Method::CreateReservation - SOAP method to create an OSCARS 
reservation.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::CreateReservation;

=head1 DESCRIPTION

SOAP method to create an OSCARS reservation.  Inherits from OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

December 22, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;
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
    $self->{resv_methods} = OSCARS::BSS::ReservationCommon->new(
                                                'user' => $self->{user},
                                                'params' => $self->{params});
    $self->{time_methods} = OSCARS::BSS::TimeConversionCommon->new(
                                                'user' => $self->{user},
                                                'params' => $self->{params});
    $self->{param_tests} = {
        'reservation_start_time' => (
            {'regexp' => '.+',
             'error' => "Please enter the reservation starting time."
            }
        ),
        'duration_hour' => (
            {'regexp' => '.+',
             'error' => "Please enter the duration in hours."
            }
        ),
        'source_host' => (
            {'regexp' => '.+',
             'error' => "Please enter starting host name or IP address."
            }
        ),
        'destination_host' => (
            {'regexp' => '.+',
             'error' => "Please enter destination host name or IP address."
            }
        ),
        'reservation_bandwidth' => (
            {'regexp' => '.+',
             'error' => "Please enter the bandwidth you wish to reserve."
            }
        ),
        'reservation_description' => (
            {'regexp' => '.+',
             'error' => "Please enter a description of the purpose for this reservation."
            }
        ),
    };
} #____________________________________________________________________________


###############################################################################
# soap_method:  SOAP call to insert a row into the reservations table. 
#     OSCARS::BSS::RouteHandler is called to set up the route before
#     inserting a reservation in the database
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub soap_method {
    my( $self ) = @_;
    my( $duration_seconds );

    # params fields having to do with traceroute modified in find_interface_ids
    $self->{route_setup}->find_interface_ids($self->{logger}, $self->{params});
    $self->{time_methods}->setup_times();

    my $pss_configs = $self->{resv_methods}->get_pss_configs();
    $self->{params}->{reservation_class} = $pss_configs->{pss_conf_CoS};
    $self->{params}->{reservation_burst_limit} =
                                     $pss_configs->{pss_conf_burst_limit};

    # convert requested bandwidth to bps
    $self->{params}->{reservation_bandwidth} *= 1000000;
    $self->check_oversubscribe();

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $self->{params}->{src_hostaddr_id} =
        $self->{resv_methods}->hostaddrs_ip_to_id($self->{params}->{source_ip}); 
    $self->{params}->{dst_hostaddr_id} =
        $self->{resv_methods}->hostaddrs_ip_to_id($self->{params}->{destination_ip}); 

    my $results = $self->get_results();
    return $results;
} #____________________________________________________________________________


#######################
# Private methods.
#######################

###############################################################################
# check_overscribe:  gets the list of active reservations at the same time as
#   this (proposed) reservation.  Also queries the db for the max speed of the
#   router interfaces to see if we have exceeded it.
#
# In: list of reservations, new reservation
# OUT: valid (0 or 1), and error message
#
sub check_oversubscribe {
    my( $self ) = @_;

    my( $reservations );
    my( %iface_idxs, $row, $reservation_path, $link, $res, $idx );
    my( $router_name );
    # maximum utilization for a particular link
    my( $max_utilization );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my( $max_reservation_utilization ) = 0.50; 

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    my $statement = 'SELECT reservation_bandwidth, reservation_start_time,
              reservation_end_time, reservation_path FROM reservations
              WHERE reservation_end_time >= ? AND
                  reservation_start_time <= ? AND ' .
                  " (reservation_status = 'pending' OR
                   reservation_status = 'active')";

    # handled query with the comparison start & end datetime strings
    $reservations = $self->{user}->do_query( $statement,
           $self->{params}->{reservation_start_time}, $self->{params}->{reservation_end_time});

    # assign the new path bandwidths 
    for $link (@{$self->{params}->{reservation_path}}) {
        $iface_idxs{$link} = $self->{params}->{reservation_bandwidth};
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
            if ($self->{params}->{form_type} eq 'admin') {
                $router_name = $self->id_to_router_name( $idx );
                $error_msg = "$router_name oversubscribed: ";
            }
            else { $error_msg = 'Route oversubscribed: '; }
            throw Error::Simple("$error_msg  $iface_idxs{$idx}" .
                  " Mbps > $max_utilization Mbps\n");
        }
    }
    # Replace array @$self->{params}->{reservation_path} with string separated by
    # spaces
    $self->{params}->{reservation_path} = join(' ', @{$self->{params}->{reservation_path}});
} #____________________________________________________________________________ 


###############################################################################
# get_interface_fields:  get the bandwidth of a router interface.
#
# IN: router interface idx
# OUT: interface row
#
sub get_interface_fields {
    my( $self, $iface_id) = @_;

    my $statement = 'SELECT * FROM interfaces WHERE interface_id = ?';
    my $row = $self->{user}->get_row($statement, $iface_id);
    return $row;
} #____________________________________________________________________________ 


###############################################################################
# get_results:  
#
sub get_results {
    my( $self ) = @_;

    # build fields to insert
    my $user_dn = $self->{user}->{dn};
    my $statement = 'SHOW COLUMNS from reservations';
    my $rows = $self->{user}->do_query( $statement );
    my @insertions;
    my $results = {}; 
    # TODO:  necessary to do insertions this way?
    for $_ ( @$rows ) {
       if ($self->{params}->{$_->{Field}}) {
           $results->{$_->{Field}} = $self->{params}->{$_->{Field}};
           push(@insertions, $self->{params}->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    # insert all fields for reservation into database
    $statement = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{user}->do_query($statement, @insertions);
    $results->{reservation_id} = $self->{user}->get_primary_id();
    # copy over non-db fields
    $results->{source_host} = $self->{params}->{source_host};
    $results->{destination_host} = $self->{params}->{destination_host};
    # clean up NULL values
    $self->{resv_methods}->check_nulls($results);
    # convert times back to user's time zone for mail message
    $self->{time_methods}->convert_times($results);

    # set user-semi-readable tag
    $results->{reservation_tag} = $user_dn . '.' .
        $self->{time_methods}->get_time_str(
              $self->{params}->{reservation_start_time}) .  "-" .
              $results->{reservation_id};
    $statement = "UPDATE reservations SET reservation_tag = ?,
                 reservation_status = 'pending'
                 WHERE reservation_id = ?";
    $unused = $self->{user}->do_query($statement,
                      $results->{reservation_tag}, $results->{reservation_id});
    # get loopback fields if have engr privileges
    # TODO:  need authorization for these fields
    $self->{resv_methods}->get_engr_fields($results); 
    $results->{reservation_tag} =~ s/@/../;
    $results->{reservation_status} = 'pending';
    return $results;
} #____________________________________________________________________________


###############################################################################
# generate_messages:  generate email message
#
sub generate_messages {
    my( $self, $resv ) = @_;

    my( @messages );
    my $user_dn = $self->{user}->{dn};
    my $msg = "Reservation scheduled by $user_dn with parameters:\n";
    $msg .= $self->{resv_methods}->reservation_stats($resv);
    my $subject_line = "Reservation scheduled by $user_dn.";
    push(@messages, { 'msg' => $msg, 'subject_line' => $subject_line, 'user' => $user_dn } ); 
    return( \@messages );
} #____________________________________________________________________________


###############################################################################
# id_to_router_name:  get the router name given the interface primary key.
# In:  interface table key id
# Out: router name
#
sub id_to_router_name {
    my( $self, $interface_id ) = @_;

    my $statement = 'SELECT router_name FROM routers
                 WHERE router_id = (SELECT router_id from interfaces
                                    WHERE interface_id = ?)';
    my $row = $self->{user}->get_row($statement, $interface_id);
    # no match
    if ( !$row ) {
        # not considered an error
        return '';
    }
    return $row->{router_name};
} #____________________________________________________________________________


# vim: et ts=4 sw=4
