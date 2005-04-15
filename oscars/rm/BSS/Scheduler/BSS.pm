######################################################################
# Main package, inclues the BSSdb for database stuff
#
# JRLee
######################################################################
package BSS ; 
use Net::Traceroute;
use Net::Ping;

# BSS data base module
use BSSdb;

# keep it tight
use strict;

### needs to be in a config file
my $use_system = 1;
my $useping = 1;


################################
### create reservaion
### 1) compute routers
### 2) check bandwidth (y2?)
### 3) return the reservation id
### IN: src, dst, start_time, duration, qos,desc, ingress_port, egress_port
### OUT: reservation id
################################
sub create_reservation {

	# XXX: should validate args
	my ($self, $src, $dst, $start_time, $duration, $qos, 
		$desc, $ingress_port, $egress_port, $dn) = @_;

    my ( $ingress_router, $egress_router, $ingress_id, $egress_id,
        $end_time, $status, $created_time, $res_id);

	($ingress_router, $egress_router) = find_router_ips($src, $dst);
	($ingress_id, $egress_id) = ip_to_interface($ingress_router, $egress_router);

    print "in $ingress_router, out $egress_router\n";

    $end_time = $start_time + $duration;
    $status = "PENDING";
    $created_time = time();
	$res_id = BSSdb::insert_db_reservation( 
        $start_time, $end_time, $qos, $status,$desc, $created_time,
        $ingress_port, $egress_port,
        $ingress_router, $egress_router,
        $dn);

    # if its 0 (zero) we failed
	return $res_id;
}

################################
### Leave the res in the db, just 
### mark it as unrunable?
################################
sub remove_reservation {
	
	# XXX: double check args
	my ($src, $dst, $start_time, $duration, $qos, 
		$desc, $ingress_port, $egress_port) = @_;
		
	my ($ingress_router, $egress_router) =
		find_router_ips($src, $dst);

	my $ingress_id = router_to_id($ingress_router);
	my $egress_id = router_to_id($egress_router);

	return 1;
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
        my  $idx = BSSdb::check_db_rtr($rtr);
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

################################
### convert the ipaddr idx to 
### to a router idx
### IN src & dst ip idx's
### OUT src and dst interface idx
################################

sub ip_to_interface {
    my ($src_idx, $dst_idx) = @_; 
    my ($src_iface, $dst_iface);

    $src_iface = BSSdb::ipaddr_to_iface_idx($src_idx);
    $dst_iface = BSSdb::ipaddr_to_iface_idx($dst_idx);

    if ($src_iface == 0 || $dst_iface == 0 ) {
        return (0,0);
    } 
	return ($src_iface, $dst_iface);
}

################################
### move to BSSdb?
################################
sub insert_db_reservation {
    return 1;
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
