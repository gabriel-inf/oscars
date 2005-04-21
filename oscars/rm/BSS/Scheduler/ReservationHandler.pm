######################################################################
# Main package, uses the BSS database front end
#
# JRLee
# DWRobertson
######################################################################

package BSS::Scheduler::ReservationHandler; 
use Net::Traceroute;
use Net::Ping;
use BSS::Traceroute::JnxTraceroute;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(create_reservation remove_reservation);

# BSS data base front end
use BSS::Frontend::Reservation;

# keep it tight
use strict;

######################### CONSTANTS ##################################

use constant LOCAL => 0;
use constant REMOTE => 1;

### needs to be in a config file
my $use_system = 1;
my $useping = 1;


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
	my ( $inref ) = @_; 

	($inref->{'ingress_router'}, $inref->{'egress_router'}) = find_interface_ids($inref->{'src_ip'}, $inref->{'dst_ip'});

    print STDERR "$inref->{'ingress_router'}, $inref->{'egress_router'}\n";

	my ($error_status, %results) = insert_reservation( $inref );
	return ($error_status, %results);
}

################################
### Leave the res in the db, just 
### mark it as unrunable?
################################
sub remove_reservation {

        # references to input arguments, output hash
	my ( $inref ) = @_;
    my ( $outref ) = \{};
    my ( $status );
		
	($outref->{'ingress_router'}, $outref->{'egress_router'}) =
		find_router_ids($inref->{'src'}, $inref->{'dst'});

	$outref->{'ingress_id'} = router_to_id($outref->{'ingress_router'});
	$outref->{'egress_id'} = router_to_id($outref->{'egress_router'});

	$status = delete_reservation( $outref );

	return ($status, $outref);
}

##############################################################
### do ping
### Freaking Net:Ping uses it own socket, so it has to be
### root to do icmp. Any smart person would turn off UDP and
### TCP echo ports ... gezzzz
##############################################################

sub do_ping {

    my $host = shift;

    # use sytem 'ping' command (should be config'd in config file
    if ($use_system) {
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

    my ($host)  = @_;;
    my (@hops);
    my ($_error, $idx, $prev);

    # try to ping before traceing?
    if ($useping) {
        if ( 0 == do_ping($host)) {
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

    print "hops: " .  $#hops . "\n";

    # if we didn't hop much, mabe the same router?
    if ($#hops < 2 ) { return 0; }

    # start off with an non-existant router
    $idx = 0;

    # loop forward till the next router isn't one of ours ...
    while(defined($hops[0]))  {
        print("  $hops[0]\n");
        $prev = $idx;
        $idx = BSS::Frontend::Database::check_db_rtr($hops[0]);
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

    
    my ($host)  = @_;;

    my ($tr, $hops);

    # try to ping before traceing?
    if ($useping) {
        if ( 0 == do_ping($host)) {
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
        my  $idx = BSS::Frontend::Database::check_db_rtr($rtr);
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
    my ($src, $dst) = @_;

    my( $ingress_rtr, $egress_rtr);

    print "running local tr to $src\n";
    $ingress_rtr = do_local_trace($src);
   
    if ( $main::config->{'run_traceroute'} )  {
        print "running remote tr to $dst\n";
        $egress_rtr = do_remote_trace($dst);
    } else {
        print "running remote (local) tr to $dst\n";
        $ingress_rtr = do_local_trace($src);
    }

    # if we can't find both ends ...
    if ($ingress_rtr == 0 || $egress_rtr == 0 ) {
        return 0,0;
    }

	return ($ingress_rtr, $egress_rtr); 
}

### last line of a module
1;
# vim: et ts=4 sw=4
