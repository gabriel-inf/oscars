##############################################################################
# Main package, uses the BSS database front end
# July 8, 2005
#
# JRLee
# DWRobertson
##############################################################################

package BSS::Scheduler::ReservationHandler; 

use Data::Dumper;
use Socket;
use Net::Ping;
use Net::Traceroute;
use Error qw(:try);

use Common::Exception;
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

    $self->{frontend} = BSS::Frontend::Reservation->new(
                                               'configs' => $self->{configs});
    $self->{dbconn} = $self->{frontend}->{dbconn};
}
######

##############################################################################
sub logout {
    my ( $self, $inref ) = @_;
		
    return $self->{dbconn}->logout( $inref->{user_dn} );
}
######

##############################################################################
# insert_reservation
# 1) compute routers
# 2) check bandwidth (y2?)
# 3) return the reservation id
# IN: ref to hash containing fields corresponding to the reservations table.
#     Some fields are still empty, and are filled in before inserting a
#     record
# OUT: hash containing all table fields, and logging buffer
#
sub insert_reservation {
    # reference to input hash ref containing fields filled in by user
    # This routine fills in the remaining fields.
    my ( $self, $inref ) = @_; 

    my $results = {};

    $self->{output_buf} = "*********************\n";
    if ($inref->{lsp_from} && $self->not_an_ip($inref->{lsp_from})) {
        $inref->{lsp_from} = inet_ntoa(inet_aton($inref->{lsp_from}));
    }
    if ($inref->{lsp_to} && $self->not_an_ip($inref->{lsp_to})) {
        $inref->{lsp_to} = inet_ntoa(inet_aton($inref->{lsp_to}));
    }
    ($inref->{ingress_interface_id}, $inref->{egress_interface_id},
            $inref->{reservation_path}) = $self->find_interface_ids($inref);

    $results  = $self->{frontend}->insert_reservation( $inref );
    $results->{reservation_tag} =~ s/@/../;
    return ( $results, $self->{output_buf} );
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
		
    return ($self->{frontend}->delete_reservation( $inref ), '');
}
######

##############################################################################
# get_reservations
# IN: ref to hash containing fields corresponding to the reservations table.
#     Some fields are still empty, and are filled in before inserting a
#     record
# OUT: 0 on success, and hash containing all table fields
#
sub get_reservations {
    my ( $self, $inref ) = @_; 

    return ($self->{frontend}->get_reservations( $inref ), '');
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
                return; 
            }
        }
        throw Common::Exception("Host $host not pingable");
    # use the Net::Ping system
    } else {
        # make sure its up and pingable first
        my $p = Net::Ping->new(proto=>'icmp');
        if (! $p->ping($host, 5) )  {
            $p->close();
            throw Common::Exception("Host $host not pingable");
        }
        $p->close();
    }
    throw Common::Exception("Host $host not pingable");
}
######

##############################################################################
# do remote trace:  Run traceroute from src to dst.
#
# In:   source, destination IP addresses.
# Out:  interface ID, path  
#
sub do_remote_trace {
    my ( $self, $user_dn, $src, $dst )  = @_;
    my (@hops);
    my ($interface_id, $prev_id, @path);
    my ($prev_loopback, $loopback_ip);

    @path = ();
    # try to ping before traceing?
    if ($self->{configs}{use_ping}) { $self->do_ping($dst); }

    my ($jnxTraceroute) = new BSS::Traceroute::JnxTraceroute();
    $jnxTraceroute->traceroute($src, $dst);
    @hops = $jnxTraceroute->get_hops();

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { throw Common::Exception("same router?"); }

    if ($#hops == 0) { 
            # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{dbconn}->ip_to_xface_id($user_dn,
                                                $self->{configs}{jnx_source});
        $loopback_ip = $self->{dbconn}->xface_id_to_loopback($user_dn,
                                                          $interface_id, 'ip');
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
        $interface_id = $self->{dbconn}->ip_to_xface_id($user_dn, $hop);
        $loopback_ip = $self->{dbconn}->xface_id_to_loopback($user_dn,
                                                     $interface_id, 'ip');
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
    throw Common::Exception("Couldn't trace route to $src");
    return;
}
######

##############################################################################
# do local trace
#
sub do_local_trace {
    my ($self, $user_dn, $host)  = @_;
    my ($tr, $hops, $interface_id);

    # try to ping before traceing?
    if ($self->{configs}{use_ping}) { $self->do_ping($host); }
    $tr = new Net::Traceroute->new( host=>$host, timeout=>30, 
            query_timeout=>3, max_ttl=>20 ) || return (0);

    if( !$tr->found ) {
        throw Common::Exception("do_local_trace: $host not found");
    } 

    $hops = $tr->hops;
    $self->{output_buf} .= "do_local_trace:  hops = $hops\n";
    print STDERR "do_local_trace:  hops = $hops\n";

    # if we didn't hop much, mabe the same router?
    if ($hops < 2 ) {
        throw Common::Exception("do_local_trace: same router?");
    }

    # loop from the last router back, till we find an edge router
    for my $i (1..$hops-1) {
        my $ipaddr = $tr->hop_query_host($hops - $i, 0);
        $interface_id = $self->{dbconn}->ip_to_xface_id($user_dn, $ipaddr);
        if ($interface_id != 0) {
            $self->{output_buf} .= "do_local_trace:  edge router is $ipaddr\n";
            print STDERR "do_local_trace:  edge router is $ipaddr\n";
            return ($interface_id);
        } 
     }
    # if we didn't find it
    throw Common::Exception("do_local_trace:  could not find edge router");
    return;
}
######

##############################################################################
# find_interface_ids:  run traceroutes to both hosts.  Find edge routers and
# validate both ends.
# IN:  src and dst host IP addresses
# OUT: ids of interfaces of the edge routers, path list (router indexes)
# TODO: validate input
#
sub find_interface_ids {
    my ($self, $inref) = @_;

    my( $src, $dst, $ingress_interface_id, $egress_interface_id );
    my( $loopback_ip, $path, $start_router );

    $src = $inref->{src_address};
    $dst = $inref->{dst_address};
    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($inref->{lsp_from}) {
        print STDERR "Ingress:  $inref->{lsp_from}\n";
        $ingress_interface_id = $self->{dbconn}->ip_to_xface_id(
                                       $inref->{user_dn}, $inref->{lsp_from});
        if ($ingress_interface_id != 0) {
            $loopback_ip = $self->{dbconn}->xface_id_to_loopback(
                                       $inref->{user_dn},
                                       $ingress_interface_id, 'ip');
        }
        else {
            throw Common::Exception(
                             "Ingress loopback is not a valid OSCARS router");
        }
    }
    else {
        $self->{output_buf} .= "--traceroute:  " .
                              "$self->{configs}{jnx_source} to source $src\n";
        ($ingress_interface_id, $loopback_ip, $path) =
                $self->do_remote_trace($inref->{user_dn},
                                       $self->{configs}{jnx_source}, $src);
    }
  
    if ($inref->{lsp_to}) {
        $egress_interface_id = $self->{dbconn}->ip_to_xface_id(
                                       $inref->{user_dn}, $inref->{lsp_to});
        if ($egress_interface_id != 0) {
            $loopback_ip = $self->{dbconn}->xface_id_to_loopback(
                                      $inref->{user_dn},
                                      $egress_interface_id, 'ip');
        }
        else {
            throw Common::Exception(
                               "Egress loopback is not a valid OSCARS router");
        }
    }
    else {
        # Use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        if ( $self->{configs}{run_traceroute} )  {
            $self->{output_buf} .= "--traceroute:  " .
                               "$loopback_ip to destination $dst\n";
            ($egress_interface_id, $loopback_ip, $path) =
                    $self->do_remote_trace($inref->{user_dn}, $loopback_ip,
                                           $dst);
        } else {
            $ingress_interface_id = $self->do_local_trace($inref->{user_dn},
                                                          $src);
        }
    }
    print "almost to end, in: $ingress_interface_id, out: $egress_interface_id\n";

    if (($ingress_interface_id == 0) || ($egress_interface_id == 0)) {
        throw Common::Exception("Unable to find route.");
    }
    print "at end, $ingress_interface_id, $egress_interface_id, $path\n";
	return ($ingress_interface_id, $egress_interface_id, $path); 
}
######

################################################################################
sub not_an_ip {
    my( $self, $form_input ) = @_;

    # simple minded for now
    my $expr = '^\d+\.\d+\.\d+\.\d+$';
    return( $form_input !~ $expr );
}
######

### last line of a module
1;
# vim: et ts=4 sw=4
