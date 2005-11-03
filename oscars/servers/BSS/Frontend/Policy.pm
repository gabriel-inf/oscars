# Policy.pm:  database handling for policy related matters
# Last modified: July 11, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Policy;

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use Common::Exception;
use BSS::Frontend::Database;

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
    my ( $self, $inref, $user_dn) = @_;

    my( $query, $sth, $reservations );
    my ( %iface_idxs, $row, $reservation_path, $link, $res, $idx );
    my ( $router_name );
    # maximum utilization for a particular link
    my ( $max_utilization );

    my $user_dn = $inref->{user_dn};
    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my ( $max_reservation_utilization ) = 0.50; 

    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    $query = "SELECT reservation_bandwidth, reservation_start_time,
              reservation_end_time, reservation_path FROM reservations
              WHERE reservation_end_time >= ? AND
                  reservation_start_time <= ? AND
                  (reservation_status = 'pending' OR
                   reservation_status = 'active')";

    # handled query with the comparison start & end datetime strings
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
           $inref->{reservation_start_time}, $inref->{reservation_end_time});
    $reservations = $sth->fetchall_arrayref({});

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
        $row = $self->get_interface_fields($user_dn, $idx);
        if (!$row ) { next; }

        if ( $row->{interface_valid} eq 'False' ) {
            throw Common::Exception("interface $idx not valid");
        }
 
        $max_utilization = $row->{interface_speed} *
                           $max_reservation_utilization;
        if ($iface_idxs{$idx} > $max_utilization) {
            my $error_msg;
            # only print router name if user has admin privileges
            if ($inref->{form_type} eq 'admin') {
                $router_name = $self->{dbconn}->xface_id_to_loopback($user_dn,
                                                                 $idx, 'name');
                $error_msg = "$router_name oversubscribed: ";
            }
            else { $error_msg = "Route oversubscribed: "; }
            throw Common::Exception($error_msg . " " . $iface_idxs{$idx} .
                  " Mbps > " .  $max_utilization . " Mbps" . "\n");
        }
    }
    # Replace array @$inref->{reservation_path} with string separated by
    # spaces
    $inref->{reservation_path} = join(' ', @{$inref->{reservation_path}});

    return;
}
######

##############################################################################
# authorized:  Given the user level string, see if the user has the required
#              privilege.
#
sub authorized {
    my( $self, $user_level, $required_priv ) = @_;
 
    for my $priv (split(' ', $user_level)) {
        if ($priv eq $required_priv) {
            return( 1 );
        }
    }
    return( 0 );
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

    my( $query, $sth, $results);

    $query = "SELECT * FROM interfaces WHERE interface_id = ?";
    $sth = $self->{dbconn}->do_query($user_dn, $query, $iface_id);
    $results = $sth->fetchrow_hashref();
    $sth->finish();

    return ( $results );
}
######

1;
# vim: et ts=4 sw=4
