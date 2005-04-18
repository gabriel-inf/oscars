######################################################################
# Main package, uses the BSS database front end
#
# JRLee
# DWRobertson
######################################################################
package BSS::Scheduler::ReservationHandler ; 
use Net::Traceroute;
use Net::Ping;

# BSS data base front end
use BSS::Frontend::Reservation;

# keep it tight
use strict;

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

       # these will be used to look up interface ids in insert_reservation
	($inref->{'ingress_router'}, $inref->{'egress_router'}) = find_router_ips($inref->{'src_ip'}, $inref->{'dst_ip'});

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
		find_router_ips($inref->{'src'}, $inref->{'dst'});

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
### do trace
################################

sub do_trace {

    my ($tr, $hops);

    my $host = shift;

    # try to ping before traceing?
    if ($useping) {
        if ( 0 == do_ping($host)) {
            print "Host not pingable\n";
            return 0;
        }
    }

    $tr = new Net::Traceroute->new(host=>$host,timeout=>30,query_timeout=>3,max_ttl=>20 ) || return 0;

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
### OUT: ips of interfaces of 
### both edge routers
### XXX: validate input
################################

sub find_router_ips {
    my ($src, $dst) = @_;

    my( $ingress_rtr, $egress_rtr);

    print "running tr to $src\n";
    $ingress_rtr = do_trace($src);
    
    print "running tr to $dst\n";
    $egress_rtr = do_trace($dst);

    # if we can't find both ends ...
    if ($ingress_rtr == 0 || $egress_rtr == 0 ) {
        return 0,0;
    }

	return ($ingress_rtr, $egress_rtr); 
}


# testing 
sub hi {

    print "HELLO 'Hi called'\n";
	my @args = @_;
	my $num = $#args;

	print "# args $num\n";	
	for my $i (@args) {
		print "==> $i\n";
	}
	# self?
	shift;
	# first arg
	my $name = shift;
	# 2nd arg
	my $baz = shift;

	# make it local, stop whining 
	#local *FOO;
	#open(FOO, '/tmp/a');
	#sleep(5);
	#$num = fileno(FOO);
	#close(F00);
	return ("hello, world [$num]{$name}{$baz}", "more stuff here", "and here");
	
}

sub bye {
	return "goodbye.";
}

### last line of a module
1;
# vim: et ts=4 sw=4
