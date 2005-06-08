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
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;

    $self->{frontend} = BSS::Frontend::Reservation->new('configs' => $self->{configs});
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
    my ( $error_status, %results );

    $self->{output_buf} = "*********************\n";
    ($inref->{ingress_interface_id}, $inref->{egress_interface_id}, $inref->{reservation_path}, $results{error_msg}) = $self->find_interface_ids($inref);

    if ($results{error_msg}) { return ( 1, %results ); }

    ( $error_status, %results ) = $self->{frontend}->insert_reservation( $inref );
    if (!$results{error_msg}) {
        $results{reservation_tag} =~ s/@/../;
    }
    else {
        $results{reservation_tag} = "fatal_reservation_errors";
    }
    open (LOGFILE, ">$ENV{OSCARS_HOME}/logs/$results{reservation_tag}") || die "Can't open log file.\n";
    print LOGFILE "********************\n";
    print LOGFILE $self->{output_buf};
    if ($results{error_msg}) {
        print LOGFILE $results{error_msg}, "\n";
    }
    close(LOGFILE);
    return ( $error_status, %results );
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
		
    my ($error_status, %results) = $self->{frontend}->delete_reservation( $inref );
    return ($error_status, %results);
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

    my ($error_status, %results) = $self->{frontend}->get_reservations( $inref );
    return ($error_status, %results);
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
    my ( $self, $src, $dst )  = @_;
    my (@hops);
    my ($error, $interface_id, $prev_id, @path);
    my ($prev_loopback, $loopback_ip);

    @path = ();
    # try to ping before traceing?
    if ($self->{configs}{use_ping}) {
        if ( 0 == $self->do_ping($dst)) {
            return (0, "", \@path, "host $dst not pingable");
        }
    }

    my ($_jnxTraceroute) = new BSS::Traceroute::JnxTraceroute();
    $error = $_jnxTraceroute->traceroute($src, $dst);
    if ($error)  {
        return (0, "", \@path, $error);
    }

    @hops = $_jnxTraceroute->get_hops();

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { return (0, "", \@path, "same router?"); }

    if ($#hops == 0) { 
            # id is 0 if not an edge router (not in interfaces table)
        ($interface_id, $error) = $self->{frontend}->{dbconn}->ip_to_xface_id($self->{configs}{jnx_source});
        if ($interface_id != 0) {
            ($loopback_ip, $error) = $self->{frontend}->{dbconn}->xface_id_to_loopback($interface_id, 'ip');
            print STDERR '-- ', $loopback_ip, "\n";
        }
        #TODO: FIX
        return ($interface_id, $loopback_ip, \@path, $error);
    }

    # start off with an non-existent router
    $interface_id = 0;

    # loop forward till the next router isn't one of ours or doesn't have
    # an oscars loopback address
    while(defined($hops[0]))  {
        $self->{output_buf} .= "hop:  $hops[0]\n";
        print STDERR "hop:  $hops[0]\n";
        # id is 0 if not an edge router (not in interfaces table)
        ($interface_id, $error) = $self->{frontend}->{dbconn}->ip_to_xface_id($hops[0]);
        # check to make sure router has a loopback
        if ($interface_id != 0) {
            ($loopback_ip, $error) = $self->{frontend}->{dbconn}->xface_id_to_loopback($interface_id, 'ip');
        }
        if ($interface_id == 0) {
            $self->{output_buf} .= "edge router is $prev_loopback\n";
            print STDERR "edge router is $prev_loopback\n";
            return ($prev_id, $prev_loopback, \@path, "");
        }

        # add to the path
        push(@path, $interface_id);
        if ($loopback_ip) {
            $prev_id = $interface_id;
            $prev_loopback = $loopback_ip;
        }
        shift(@hops);
    }

    # if we didn't find it
    return (0, "", \@path, "Couldn't trace route to $src");
}
######

##############################################################################
# do local trace
#
sub do_local_trace {
    my ($self, $host)  = @_;
    my ($tr, $hops, $interface_id, $error);

    # try to ping before traceing?
    if ($self->{configs}{use_ping}) {
        if ( 0 == $self->do_ping($host)) {
            return (0, "do_local_trace: Host $host not pingable");
        }
    }

    $tr = new Net::Traceroute->new( host=>$host, timeout=>30, 
            query_timeout=>3, max_ttl=>20 ) || return 0;

    if( ! $tr->found) {
        return (0, "do_local_trace:  $host not found");
    } 

    $hops = $tr->hops;
    $self->{output_buf} .= "do_local_trace:  hops = $hops\n";
    print STDERR "do_local_trace:  hops = $hops\n";

    # if we didn't hop much, mabe the same router?
    if ($hops < 2 ) { return (0, "do_local_trace:  same router?"); }

    # loop from the last router back, till we find an edge router
    for my $i (1..$hops-1) {
        my $ipaddr = $tr->hop_query_host($hops - $i, 0);
        ($interface_id, $error) = $self->{frontend}->{dbconn}->ip_to_xface_id($ipaddr);
        if (($interface_id != 0) && (!$error)) {
            $self->{output_buf} .= "do_local_trace:  edge router is $ipaddr\n";
            print STDERR "do_local_trace:  edge router is $ipaddr\n";
            return ($interface_id, "");
        } 
        if ($error) { return (0, $error); }
     }
    # if we didn't find it
    return (0, "do_local_trace:  could not find edge router");
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
    my( $loopback_ip, $path, $err_msg, $start_router );

    $src = $inref->{src_hostaddrs_ip};
    $dst = $inref->{dst_hostaddrs_ip};
    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($inref->{lsp_from}) {
        ($ingress_interface_id, $err_msg) = $self->{frontend}->{dbconn}->ip_to_xface_id($inref->{lsp_from});
    }
    else {
        $self->{output_buf} .= "--traceroute:  $self->{configs}{jnx_source} to source $src\n";
        ($ingress_interface_id, $loopback_ip, $path, $err_msg) = $self->do_remote_trace(
                                               $self->{configs}{jnx_source}, $src);
    }
    if ($err_msg) { return ( 0, 0, $err_msg); }
  
    if ($inref->{lsp_to}) {
        ($egress_interface_id, $err_msg) = $self->{frontend}->{dbconn}->ip_to_xface_id($inref->{lsp_to});
    }
    else {
        # Now use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        if ( $self->{configs}{run_traceroute} )  {
            $self->{output_buf} .= "--traceroute:  $loopback_ip to destination $dst\n";
            ($egress_interface_id, $loopback_ip, $path, $err_msg) = $self->do_remote_trace(
                                               $loopback_ip, $dst);
        } else {
            ($ingress_interface_id, $err_msg) = $self->do_local_trace($src);
        }
    }
    if ($err_msg) { return (0, 0, $err_msg); }

    if (($ingress_interface_id == 0) || ($egress_interface_id == 0))
    {
        return( 0, 0, 0, "Unable to find route." );
    }
	return ($ingress_interface_id, $egress_interface_id, $path, ""); 
}
######

### last line of a module
1;
# vim: et ts=4 sw=4
