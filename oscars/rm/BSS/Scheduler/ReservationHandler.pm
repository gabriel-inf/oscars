##############################################################################
# Main package, uses the BSS database front end
#
# JRLee
# DWRobertson
##############################################################################

package BSS::Scheduler::ReservationHandler; 

use Data::Dumper;

use Net::Ping;

use Net::Traceroute;
use BSS::Traceroute::JnxTraceroute;

# BSS data base front end
use BSS::Frontend::Reservation;

use strict;


##############################################################################
sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $self into designated class.
    bless($self, $class);
  
    # Initialize.
    $self->initialize();

    return($self);
}

sub initialize {
    my ($self) = @_;

    $self->{frontend} = BSS::Frontend::Reservation->new('configs' => $self->{configs});
    # clear error message
    $self->{error_msg} = 0;
}
######

##############################################################################
# get_error:  Return the error message (0 if none).
# In:  <none>
# Out: Error message
#
sub get_error {
    my ($self) = @_;

    return($self->{error_msg});
}
######


##############################################################################
sub logout {
    my ( $self, $inref ) = @_;
		
    return $self->{frontend}->{dbconn}->logout( $inref->{user_dn} );
}
######

##############################################################################
# create reservation
# 1) compute routers
# 2) check bandwidth (y2?)
# 3) return the reservation id
# IN: ref to hash containing fields corresponding to the reservations table.
#     Some fields are still empty, and are filled in before inserting a
#     record
# OUT: 0 on success, and hash containing all table fields, as well as error
#     or status message
#
sub create_reservation {
    # reference to input hash ref containing fields filled in by user
    # This routine fills in the remaining fields.
    my ( $self, $inref ) = @_; 

    my ( $error_status );
    my $results = {};

    $self->{output_buf} = "*********************\n";
    ($inref->{ingress_interface_id}, $inref->{egress_interface_id}, $inref->{reservation_path}) = $self->find_interface_ids($inref);
    $results->{error_msg} = $self->get_error();

    if ($results->{error_msg}) { return ( $results ); }

    $results  = $self->{frontend}->insert_reservation( $inref );
    if (!$results->{error_msg}) {
        $results->{reservation_tag} =~ s/@/../;
    }
    else {
        $results->{reservation_tag} = "fatal_reservation_errors";
    }
    open (LOGFILE, ">$ENV{OSCARS_HOME}/logs/$results->{reservation_tag}") || die "Can't open log file.\n";
    print LOGFILE "********************\n";
    print LOGFILE $self->{output_buf};
    if ($results->{error_msg}) {
        print LOGFILE $results->{error_msg}, "\n";
    }
    close(LOGFILE);
    return ( $results );
}
######

##############################################################################
# delete_reservation:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub delete_reservation {
    my ( $self, $inref ) = @_;
		
    return $self->{frontend}->delete_reservation( $inref );
}
######

##############################################################################
# get_reservations
# IN: ref to hash containing fields corresponding to the reservations table.
#     Some fields are still empty, and are filled in before inserting a
#     record
# OUT: 0 on success, and hash containing all table fields, as well as error
#     or status message
#
sub get_reservations {
    my ( $self, $inref ) = @_; 

    return $self->{frontend}->get_reservations( $inref );
}
######

##############################################################################
# do ping:
# Freaking Net:Ping uses it own socket, so it has to be
# root to do icmp. Any smart person would turn off UDP and
# TCP echo ports ... gezzzz
#
sub do_ping {
    my ( $self, $host ) = @_;

    # use sytem 'ping' command (should be config'd in config file
    if ($self->{configs}{use_system}) {
        my @ping = `/bin/ping -w 10 -c 3 -n  $host`;
        for my $i (@ping) {
            if ( $i =~ /^64 bytes/ ) {  
                return 1; 
            }
        }
        return 0;
    # use the Net::Ping system
    } else {
        # make sure its up and pingable first
        my $p = Net::Ping->new(proto=>'icmp');
        if (! $p->ping($host, 5) )  {
            $p->close();
            return 0;
        }
        $p->close();
    }
    return 0;
}
######

##############################################################################
# do remote trace:  Run traceroute from src to dst.
#
# In:   source, destination IP addresses.
# Out:  interface ID, path, and error message, if any  
#
sub do_remote_trace {
    my ( $self, $user_dn, $src, $dst )  = @_;
    my (@hops);
    my ($interface_id, $prev_id, @path);
    my ($prev_loopback, $loopback_ip);

    $self->{error_msg} = 0;
    @path = ();
    # try to ping before traceing?
    if ($self->{configs}{use_ping}) {
        if ( 0 == $self->do_ping($dst)) {
            $self->{error_msg} = "host $dst not pingable";
            return (0, "", \@path);
        }
    }

    my ($jnxTraceroute) = new BSS::Traceroute::JnxTraceroute();
    if (!$jnxTraceroute->traceroute($src, $dst)) {
        $self->{error_msg} = $jnxTraceroute->get_error();
        return (0, "", \@path);
    }

    @hops = $jnxTraceroute->get_hops();

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) {
        $self->{error_msg} = "same router?";
        return (0, "", \@path);
    }

    if ($#hops == 0) { 
            # id is 0 if not an edge router (not in interfaces table)
        ($interface_id, $self->{error_msg}) = $self->{frontend}->{dbconn}->ip_to_xface_id($user_dn, $self->{configs}{jnx_source});
        if ($self->{error_msg})  { return (0, "", \@path); }

        if ($interface_id != 0) {
            ($loopback_ip, $self->{error_msg}) = $self->{frontend}->{dbconn}->xface_id_to_loopback($user_dn, $interface_id, 'ip');
            if ($self->{error_msg})  { return (0, "", \@path); }
        }
        return ($interface_id, $loopback_ip, \@path);
    }

    # start off with an non-existent router
    $interface_id = 0;
    # loop forward till the next router isn't one of ours or doesn't have
    # an oscars loopback address
    my $hop;
    for $hop (@hops)  {
        $self->{output_buf} .= "hop:  $hop\n";
        print STDERR "hop:  $hop\n";
        # id is 0 if not an edge router (not in interfaces table)
        ($interface_id, $self->{error_msg}) = $self->{frontend}->{dbconn}->ip_to_xface_id($user_dn, $hop);
        if ($self->{error_msg})  {
            return (0, "", \@path);
        }
        # check to make sure router has a loopback
        if ($interface_id != 0) {
            ($loopback_ip, $self->{error_msg}) = $self->{frontend}->{dbconn}->xface_id_to_loopback($user_dn, $interface_id, 'ip');
            if ($self->{error_msg})  {
                return (0, "", \@path);
            }
        }
        if ($interface_id == 0) {
            $self->{output_buf} .= "edge router is $prev_loopback\n";
            print STDERR "edge router is $prev_loopback\n";
            return ($prev_id, $prev_loopback, \@path);
        }

        # add to the path
        push(@path, $interface_id);
        if ($loopback_ip && ($loopback_ip != 'NULL')) {
            $prev_id = $interface_id;
            $prev_loopback = $loopback_ip;
        }
    }
    # Need this in case the last hop is in the database
    if ($prev_loopback) {
        $self->{output_buf} .= "edge router is $prev_loopback\n";
        print STDERR "edge router is $prev_loopback\n";
        return ($prev_id, $prev_loopback, \@path);
    }

    # if we didn't find it
    $self->{error_msg} = "Couldn't trace route to $src";
    return (0, "", \@path);
}
######

##############################################################################
# do local trace
#
sub do_local_trace {
    my ($self, $user_dn, $host)  = @_;
    my ($tr, $hops, $interface_id);

    $self->{error_msg} = 0;
    # try to ping before traceing?
    if ($self->{configs}{use_ping}) {
        if ( 0 == $self->do_ping($host)) {
            $self->{error_msg} = "do_local_trace: Host $host not pingable";
            return (0);
        }
    }

    $tr = new Net::Traceroute->new( host=>$host, timeout=>30, 
            query_timeout=>3, max_ttl=>20 ) || return (0);

    if( !$tr->found ) {
        $self->{error_msg} = "do_local_trace: $host not found";
        return (0);
    } 

    $hops = $tr->hops;
    $self->{output_buf} .= "do_local_trace:  hops = $hops\n";
    print STDERR "do_local_trace:  hops = $hops\n";

    # if we didn't hop much, mabe the same router?
    if ($hops < 2 ) {
        $self->{error_msg} = "do_local_trace: same router?";
        return (0);
    }

    # loop from the last router back, till we find an edge router
    for my $i (1..$hops-1) {
        my $ipaddr = $tr->hop_query_host($hops - $i, 0);
        ($interface_id, $self->{error_msg}) = $self->{frontend}->{dbconn}->ip_to_xface_id($user_dn, $ipaddr);
        if (($interface_id != 0) && (!$self->{error_msg})) {
            $self->{output_buf} .= "do_local_trace:  edge router is $ipaddr\n";
            print STDERR "do_local_trace:  edge router is $ipaddr\n";
            return ($interface_id);
        } 
        if ($self->{error_msg}) {
            return (0);
        }
     }
    # if we didn't find it
    $self->{error_msg} = "do_local_trace:  could not find edge router";
    return (0);
}
######

##############################################################################
# find_interface_ids:  run traceroutes to both hosts.  Find edge routers and
# validate both ends.
# IN:  src and dst host IP addresses
# OUT: ids of interfaces of the edge routers, path list (router indexes), and
#      error msg
# TODO: validate input
#
sub find_interface_ids {
    my ($self, $inref) = @_;

    my( $src, $dst, $ingress_interface_id, $egress_interface_id );
    my( $loopback_ip, $path, $start_router );

    $self->{error_msg} = 0;
    $src = $inref->{src_address};
    $dst = $inref->{dst_address};
    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($inref->{lsp_from}) {
        print STDERR "Ingress:  $inref->{lsp_from}\n";
        ($ingress_interface_id, $self->{error_msg}) = $self->{frontend}->{dbconn}->ip_to_xface_id($inref->{user_dn}, $inref->{lsp_from});
        if ($ingress_interface_id != 0) {
            ($loopback_ip, $self->{error_msg}) = $self->{frontend}->{dbconn}->xface_id_to_loopback(
                                          $inref->{user_dn},
                                          $ingress_interface_id, 'ip');
            if ($self->{error_msg})  { return (0, "", $path); }
        }
        else {
            $self->{error_msg} = "Ingress loopback is not a valid OSCARS router";
            return (0, "", $path);
        }
    }
    else {
        $self->{output_buf} .= "--traceroute:  $self->{configs}{jnx_source} to source $src\n";
        ($ingress_interface_id, $loopback_ip, $path) =
                $self->do_remote_trace($inref->{user_dn}, $self->{configs}{jnx_source}, $src);
    }
    if ($self->get_error()) { return ( 0, 0, $path); }
  
    if ($inref->{lsp_to}) {
        ($egress_interface_id, $self->{error_msg}) = $self->{frontend}->{dbconn}->ip_to_xface_id(
                                          $inref->{user_dn}, $inref->{lsp_to});
        if ($egress_interface_id != 0) {
            ($loopback_ip, $self->{error_msg}) = $self->{frontend}->{dbconn}->xface_id_to_loopback(
                                          $inref->{user_dn},
                                          $egress_interface_id, 'ip');
            if ($self->{error_msg})  { return (0, "", $path); }
        }
        else {
            $self->{error_msg} = "Egress loopback is not a valid OSCARS router";
            return (0, "", $path);
        }
    }
    else {
        # Use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        if ( $self->{configs}{run_traceroute} )  {
            $self->{output_buf} .= "--traceroute:  $loopback_ip to destination $dst\n";
            ($egress_interface_id, $loopback_ip, $path) = $self->do_remote_trace($inref->{user_dn},
                                               $loopback_ip, $dst);
        } else {
            $ingress_interface_id = $self->do_local_trace($inref->{user_dn}, $src);
        }
    }
    if ($self->{error_msg}) { return (0, 0, $path); }

    if (($ingress_interface_id == 0) || ($egress_interface_id == 0)) {
        $self->{error_msg} = "Unable to find route.";
        return( 0, 0, $path );
    }
	return ($ingress_interface_id, $egress_interface_id, $path); 
}
######

### last line of a module
1;
# vim: et ts=4 sw=4
