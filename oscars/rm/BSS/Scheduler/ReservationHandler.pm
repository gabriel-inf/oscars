######################################################################
# Main package, uses the BSS database front end
#
# JRLee
# DWRobertson
######################################################################

package BSS::Scheduler::ReservationHandler; 

use Net::Ping;

use Net::Traceroute;
use BSS::Traceroute::JnxTraceroute;

# BSS data base front end
use BSS::Frontend::Reservation;

# keep it tight
use strict;


######################################################################
sub new {
  my ($_class, %_args) = @_;
  my ($_self) = {%_args};
  
  # Bless $_self into designated class.
  bless($_self, $_class);
  
  # Initialize.
  $_self->initialize();
  
  return($_self);
}

use Data::Dumper;

######################################################################
sub initialize {
    my ($self) = @_;

    $self->{'frontend'} = BSS::Frontend::Reservation->new('configs' => $self->{'configs'});
}


######################################################################

sub db_login {
    my ($self) = @_;
    $self->{'frontend'}->db_login();
}

################################
### create reservation
### 1) compute routers
### 2) check bandwidth (y2?)
### 3) return the reservation id
### IN: ref to hash containing fields corresponding to the reservations table.
###     Some fields are still empty, and are filled in before inserting a
###     record
### OUT: 0 on success, and hash containing all table fields, as well as error
###     or status message
################################
sub create_reservation {

        # reference to input hash ref containing fields filled in by user
        # This routine fills in the remaining fields.
	my ( $self, $inref ) = @_; 

	($inref->{'ingress_router'}, $inref->{'egress_router'}) = $self->find_interface_ids($inref->{'src_ip'}, $inref->{'dst_ip'});

	my ($error_status, %results) = $self->{'frontend'}->insert_reservation( $inref );
	return ($error_status, %results);
}

################################
### Leave the res in the db, just 
### mark it as unrunable?
################################
sub remove_reservation {

        # references to input arguments, output hash
	my ( $self, $inref ) = @_;
    my ( $outref ) = \{};
    my ( $status );
		
	($outref->{'ingress_router'}, $outref->{'egress_router'}) =
		$self->{'frontend'}->{'dbconn'}->find_router_ids($inref->{'src'}, $inref->{'dst'});

	$outref->{'ingress_id'} = $self->{'frontend'}->{'dbconn'}->router_to_id($outref->{'ingress_router'});
	$outref->{'egress_id'} = $self->{'frontend'}->{'dbconn'}->router_to_id($outref->{'egress_router'});

	$status = $self->{'frontend'}->delete_reservation( $outref );

	return ($status, $outref);
}

################################
### get_reservations
### IN: ref to hash containing fields corresponding to the reservations table.
###     Some fields are still empty, and are filled in before inserting a
###     record
### OUT: 0 on success, and hash containing all table fields, as well as error
###     or status message
################################
sub get_reservations {

        # reference to input hash ref containing fields filled in by user
        # This routine fills in the remaining fields.
	my ( $self, $inref, $fields_to_display ) = @_; 

	my ($error_status, %results) = $self->{'frontend'}->get_reservations( $inref, $fields_to_display );
	return ($error_status, %results);
}

##############################################################
### do ping
### Freaking Net:Ping uses it own socket, so it has to be
### root to do icmp. Any smart person would turn off UDP and
### TCP echo ports ... gezzzz
##############################################################

sub do_ping {
    my ( $self, $host ) = @_;

    # use sytem 'ping' command (should be config'd in config file
    if ($self->{'configs'}{'use_system'}) {
        my @ping = `/bin/ping -w 10 -c 3 -n  $host`;
        foreach my $i (@ping) {
            if ( $i =~ /^64 bytes/ ) {  
                return 1; 
            }
        }
        print "Counld not ping $host\n";
        return 0;
    # use the Net::Ping system
    } else {
        # make sure its up and pingable first
        my $p = Net::Ping->new(proto=>'icmp');
        if (! $p->ping($host, 5) )  {
            print "Counld not ping $host\n";
            $p->close();
            return 0;
        }
        $p->close();
    }
    print "end do ping\n";
    return 0;
}

################################
### do remote trace
################################

sub do_remote_trace {
    my ( $self, $host )  = @_;
    my (@hops);
    my ($_error, $idx, $prev);

    # try to ping before traceing?
    if ($self->{'configs'}{'use_ping'}) {
        if ( 0 == $self->do_ping($host)) {
            print "Host not pingable\n";
            return 0;
        }
    }

    my ($_jnxTraceroute) = new JnxTraceroute();
    $_jnxTraceroute->traceroute($host);

    if ($_error = $_jnxTraceroute->get_error())  {
        print "Error $_error\n";
        return 0;
    }

    @hops = $_jnxTraceroute->get_hops();

    print "hops: " .  $#hops + 1 . "\n";

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { return 0; }

    if ($#hops == 0) { 
        print "returning " . $_jnxTraceroute->{'defaultrouter'} . "\n";
        $idx = $self->{'frontend'}->{'dbconn'}->check_db_rtr($_jnxTraceroute->{'defaultrouter'});
        return $idx;
    }

    # start off with an non-existant router
    $idx = 0;

    # loop forward till the next router isn't one of ours ...
    while(defined($hops[0]))  {
        print("hop:  $hops[0]\n");
        $prev = $idx;
        $idx = $self->{'frontend'}->{'dbconn'}->check_db_rtr($hops[0]);
        if ( $idx == 0 ) {
            return $prev;
        }
        shift(@hops);
    }

    # if we didn't find it
    return 0;
}

################################
### do local trace
################################

sub do_local_trace {
    my ($self, $host)  = @_;
    my ($tr, $hops);

    # try to ping before traceing?
    if ($self->{'configs'}{'use_ping'}) {
        if ( 0 == $self->do_ping($host)) {
            print "Host not pingable\n";
            return 0;
        }
    }

    $tr = new Net::Traceroute->new( host=>$host, timeout=>30, 
            query_timeout=>3, max_ttl=>20 ) || return 0;

    if( ! $tr->found) {
        print "do_trace:'$host' not found\n";
        return 0;
    } 

    $hops = $tr->hops;
    print "hops = $hops\n";

    # if we didn't hop much, mabe the same router?
    if ($hops < 2 ) { return 0; }

    # loop from the last router back, till we find an edge router
    for my $i (1..$hops-1) {
        my $rtr = $tr->hop_query_host($hops - $i, 0);
        my  $idx = $self->{'frontend'}->{'dbconn'}->check_db_rtr($rtr);
        if ($idx != 0) {
            #print "FOUND idx = $idx\n";
            return $idx;
        } 
     }
    # if we didn't find it
    return 0;
}
################################
### run traceroutes to both hosts
### find edge routers validate both
### ends
### IN: src and dst hosts
### OUT: ids of interfaces of 
### both edge routers
### XXX: validate input
################################

sub find_interface_ids {
    my ($self, $src, $dst) = @_;

    my( $ingress_rtr, $egress_rtr);

    print "running local tr to $src\n";
    #$ingress_rtr = $self->do_local_trace($src);
    $ingress_rtr = $self->do_remote_trace($src);
   
    if ( $self->{'configs'}{'run_traceroute'} )  {
        print "running remote tr to $dst\n";
        $egress_rtr = $self->do_remote_trace($dst);
    } else {
        print "running remote (local) tr to $dst\n";
        $ingress_rtr = $self->do_local_trace($src);
    }
    print "ingress: ", $ingress_rtr, " egress: ", $egress_rtr, "\n";

    # if we can't find both ends ...
    if ($ingress_rtr == 0 || $egress_rtr == 0 ) {
        return 0,0;
    }

	return ($ingress_rtr, $egress_rtr); 
}

### last line of a module
1;
# vim: et ts=4 sw=4
