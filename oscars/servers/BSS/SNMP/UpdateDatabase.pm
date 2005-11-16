package BSS::SNMP::UpdateDatabase;

# Reads ifrefpoll files to update database with latest router and interface 
# information
#
# Last modified:   November 15, 2005
# David Robertson  (dwrobertson@lbl.gov)
# Jason Lee        (jrlee@lbl.gov)


use strict;

use File::Basename;
use Data::Dumper;

use BSS::Frontend::DBRequests;

###############################################################################
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{dbconn} = BSS::Frontend::DBRequests->new(
                              'database' => 'DBI:mysql:BSSTest',
                              'dblogin' => 'oscars',
                              'password' => 'ritazza6')
                      or die "FATAL:  could not connect to database";
}
######

##############################################################################
# update_router_info: 
#
# Gets latest SNMP data to update routers, interfaces, and 
# ipaddrs table Currently only inserts on empty tables are tested.
# 
sub update_router_info {
    my( $self, $params ) = @_;

    my( @file_list, $routers, $interfaces );

    chdir($params->{directory});
    opendir(DATADIR, ".") or die "Directory: $params->{directory}: $!";
    # For now, assumes all files are data files
    @file_list = grep { $_ ne '.' and $_ ne '..' } readdir(DATADIR);
    closedir(DATADIR);
    ($routers, $interfaces) = $self->read_snmp_files(@file_list);
    #$self->update_db($routers, $interfaces);
}
######


##############################################################################
# read_snmp_files:  reads list of SNMP output files
#
# In:   list of file names
# Out:  refs to two hashes; each hash is keyed by the router name associated
#       with it.
#
sub read_snmp_files {
    my( $self, @file_list ) = @_;

    my( %interfaces, %routers, $router_name, $router_info, $interface_info );
    my( $path, $suffix );

    my @suffixlist = (".out");
    for my $fname (@file_list) {
        ($router_info, $interface_info) = $self->read_snmp_data($fname);
        ($router_name, $path, $suffix) = fileparse($fname,@suffixlist);
        print STDERR "foo: $router_name\n";
        $routers{$router_name} = $router_info;
        $interfaces{$router_name} = $interface_info;
        print Dumper($interfaces{$router_name});
    }
    return( \%routers, \%interfaces );
}
######

##############################################################################
# read_snmp_data:  Reads data for one snmp output file.
#
# It assumes snmp output, for example
#  time              var                   data
# 1117523019     ifDescr.24                gr-0/0/0
# 1117523019     ipAdEntIfIndex.127.0.0.1  16
#
# Two cases are handled.  If a row is an ipAdEntIfIndex entry, the second
# part of the "var" field (the IP address) is used as a key into a hash 
# (routers), and the data field (the interface index) is the hash value.
#
# In the other case, the interface number is used as an index into an array
# of hashes (interfaces).  The first portion of the "var" field is used as
# the hash key, and the "data" field is the value.  The interface index is
# the second portion of the "var" field.
#
# In:   file name
# Out:  ref to hash keying IP address to interface index, and ref to array of
#       hashes indexed by interface index
#
sub read_snmp_data {
    my( $self, $fname ) = @_;

    my( $time_ctr, %router_info, @interface_info, @fields );

    open SNMP_OUT, $fname or die("Unable to open: $!\n");
    $time_ctr = 0;
    while(<SNMP_OUT>) {
        chomp;
        if (/sysUpTimeInstance./) {
            $time_ctr++ ;
            # ignore subsequent times (delineated by sysUpTimeInstance)
            if ($time_ctr > 1) { last; }
            next;
        }
        @fields = split;
        if (!$fields[1]) { next; }
        #  TODO:  error checking
        if ($fields[1] =~ /(ipAdEntIfIndex.)([\d\.]*)/) {
            $router_info{$2} = $fields[2];
        }
        elsif ($fields[1] =~ /(\w+\.)([\d\.]*)/) {
            $interface_info[$2]{substr($1, 0, -1)} = $fields[2];
        }
    }
    close(SNMP_OUT);
    return( \%router_info, \@interface_info);
}
######

##############################################################################
# update_db:  Compares current info with routers, interfaces, and ipaddrs
#             tables.
#
# In:   db instance, and two hashes keyed by router name
# Out:  Error if any
#
sub update_db {
    my( $self, $routers, $interfaces ) = @_;

    my( $router_id, $mpls_loopback );

    # for now (ESnet)
    my $network_id = 1;
    for my $router_name (sort keys %$routers) {   # sort by router name
        print STDERR '** ', $router_name, "\n";
        $mpls_loopback = undef;
        for my $ipaddr (sort keys %{$routers->{$router_name}}) {    # sort by IP
            if ($ipaddr =~ /134\.55\.75\.*/) {
                 $mpls_loopback = $ipaddr;
                 last;
            }
        }
        if ($mpls_loopback) {
            $router_id = $self->update_routers_table($network_id, $router_name,
                                                     $mpls_loopback);
            $self->update_xfaces_table($router_id, $interfaces->{router_name});
        }
    } 
}
######

##############################################################################
# update_routers_table: Compares router name and loopback with routers table,
#                       and does inserts and updates as necessary.
#
# In:   router name, router MPLS loopback address
# Out:  primary key, and error message, if any
#
sub update_routers_table {
    my( $self, $network_id, $router_name, $mpls_loopback ) = @_;

    my( $router_id, $unused );

    my $statement = "SELECT router_id, router_name, router_loopback
                     FROM routers WHERE router_name = ?";
    my $row = $self->{dbconn}->get_row($statement, $router_name);
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into routers VALUES ( NULL, True,
                     '$router_name', '$mpls_loopback',
                     $network_id)";
        $unused = $self->{dbconn}->do_query($statement);
        $router_id = $self->{dbconn}->{dbh}->{mysql_insertid};
        return( $router_id );
    }
    $router_id = $row->{router_id};
    if ($row->{router_loopback} ne $mpls_loopback) {
        $statement = "UPDATE routers SET router_loopback = ?
                      WHERE router_id = ?";
        $unused = $self->{dbconn}->do_query($statement, $mpls_loopback, $router_id);
    }
    return( $router_id );
}
######

##############################################################################
# update_xfaces_table:  Compares current row with interfaces table, and does
#                       inserts and updates if necessary.
#
# In:   router id, and ref to hash containing interface fields
# Out:  primary key in interfaces, and error message, if any
#
sub update_xfaces_table {
    my( $self, $router_id, $xface ) = @_;

    my( $interface_id, $unused );

    my $statement = "SELECT interface_id, interface_speed, interface_descr,
                     interface_alias from interfaces
                     WHERE router_id = ?";
    my $row = $self->{dbconn}->get_row($statement, $router_id);
    # calculate bandwidth given by new data
    my $new_speed = 0;
    if (defined($xface->{ifSpeed})) {
        $new_speed = $xface->{ifSpeed};
    }
    if (defined($xface->{ifHighSpeed})) {
        if ($new_speed < ($xface->{ifHighSpeed} * 1000000)) {
            $new_speed = $xface->{ifHighSpeed} * 1000000;
        }
    }
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into interfaces VALUES ( NULL, True,
                  $new_speed, '$xface->{ifDescr}', '$xface->{ifAlias}',
                  $router_id)";
        $unused = $self->{dbconn}->do_query($statement);
        $interface_id = $self->{dbconn}->{dbh}->{mysql_insertid};
        return( $interface_id );
    }
    $interface_id = $row->{interface_id};
    if (($row->{interface_speed} != $new_speed) ||
        ($row->{interface_descr} != $xface->{ifDescr}) ||
        ($row->{interface_alias} != $xface->{ifAlias})) {
        $statement = "UPDATE interfaces SET interface_speed = ?,
                  interface_descr = ?, interface_alias = ?
                  WHERE interface_id = ?";
        $unused = $self->{dbconn}->do_query($statement, $new_speed,
                                 $xface->{ifDescr}, $xface->{ifAlias},
                                 $interface_id);
    }
    return( $interface_id );
}
######

##############################################################################
# update_ipaddrs_table:  Compares current row with ipaddrs table, and does
#                        inserts and updates if necessary.
#
# In:   interface id, and interface IP address
# Out:  primary key in ipaddrs, and error message, if any
#
sub update_ipaddrs_table {

    my( $self, $interface_id, $interface_ip ) = @_;
    my( $ipaddrs_id, $unused );

    # TODO:  handling case where interface_id is different?
    my $statement = "SELECT ipaddrs_id, ipaddrs_ip FROM ipaddrs
                     WHERE ipaddrs_ip = ?";
    my $row = $self->{dbconn}->get_row($statement, $interface_ip);
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into ipaddrs VALUES ( NULL, $interface_ip,
                  $interface_id)";
        $unused = $self->{dbconn}->do_query($statement);
        $ipaddrs_id = $self->{dbconn}->{dbh}->{mysql_insertid};
        return( $ipaddrs_id );
    }
    $ipaddrs_id = $row->{ipaddrs_id};
    return( $ipaddrs_id );
}
######


1;
