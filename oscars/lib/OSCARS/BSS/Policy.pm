###############################################################################
package OSCARS::BSS::Policy;

# Database requests for policy related matters.
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)


use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use OSCARS::BSS::Database;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
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
    my( $self, $params) = @_;

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
    $reservations = $self->{dbconn}->do_query( $statement,
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
                $router_name = $self->{dbconn}->id_to_router_name( $idx );
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


#################
# Private methods
#################

###############################################################################
# get_interface_fields:  get the bandwidth of a router interface.
#
# IN: router interface idx
# OUT: interface row
#
sub get_interface_fields {
    my( $self, $iface_id) = @_;

    my $statement = 'SELECT * FROM interfaces WHERE interface_id = ?';
    my $row = $self->{dbconn}->get_row($statement, $iface_id);
    return $row;
} #____________________________________________________________________________ 


1;
# vim: et ts=4 sw=4
