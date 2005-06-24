# Reservation.pm:  database handling for policy related matters
# Last modified: June 15, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Policy;

use strict;

use DBI;
use Data::Dumper;

###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($self) = {%_args};
  
    # Bless $self into designated class.
    bless($self, $_class);
  
    # Initialize.
    $self->initialize();
  
    return($self);
}

sub initialize {
    my ($self) = @_;
}

######


###############################################################################
# check_overscribe:  gets the list of active reservations at the same time as
#   this (proposed) reservation.  Also queries the db for the max speed of the
#   router interfaces to see if we have exceeded it.
#
# In: list of reservations, new reservation
# OUT: valid (0 or 1), and error message
#
sub check_oversubscribe {
    my ( $self, $reservations, $inref) = @_;

    my ( %iface_idxs, $row, $reservation_path, $link, $res, $idx );
    my ( $router_name, $error_msg );
    my $user_dn = $inref->{user_dn};

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my ( $max_reservation_utilization) = 0.50; 

    # assign the new path bandwidths 
    for $link (@{$inref->{reservation_path}}) {
        $iface_idxs{$link} = $inref->{reservation_bandwidth};
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
        ($row, $error_msg) = $self->get_interface_fields($user_dn, $idx);
        if ($error_msg) { return (1, $error_msg); }
        if (!$row ) { next; }

        if ( $row->{interface_valid} == 'False' ) {
            return ( 1, "interface $idx not valid");
        }
 
        if ($iface_idxs{$idx} >
            ($row->{interface_speed} * $max_reservation_utilization)) {
            my $max_utilization = $row->{interface_speed} * $max_reservation_utilization/1000000.0;
            if ($inref->{form_type} eq 'admin') {
                ($router_name, $error_msg) = $self->{dbconn}->xface_id_to_loopback($user_dn, $idx, 'name');
                if ($error_msg) { return (1, $error_msg); }
                $error_msg = "$router_name oversubscribed: ";
            }
            else {
                $error_msg = "Route oversubscribed: ";
            }
            return ( 1, $error_msg . $iface_idxs{$idx}/1000000 . " Mbps > " . "$max_utilization Mbps" );
        }
    }
    # Replace array @$inref->{reservation_path} with string separated by
    # spaces
    $inref->{reservation_path} = join(' ', @{$inref->{reservation_path}});
    return (0, "");
}
######

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
    my( $self, $user_dn, $iface_id) = @_;

    my( $error_msg, $query, $sth, $results);

    $query = "SELECT * FROM interfaces WHERE interface_id = ?";
    ($sth, $error_msg) = $self->{dbconn}->do_query($user_dn, $query, $iface_id);
    if ($error_msg) { return ( undef, $error_msg ); }

    $results = $sth->fetchrow_hashref();
    $sth->finish();

    return ( $results, '');
}
######

1;
# vim: et ts=4 sw=4
