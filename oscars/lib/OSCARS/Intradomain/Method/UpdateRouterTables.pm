###############################################################################
package OSCARS::Intradomain::Method::UpdateRouterTables;

=head1 NAME

OSCARS::Intradomain::Method::UpdateRouterTables - SOAP method to update router-associated tables.

=head1 SYNOPSIS

  use OSCARS::Intradomain::Method::UpdateRouterTables;

=head1 DESCRIPTION

SOAP method to update the routers, interfaces, and ipaddrs Intradomain
database tables given the current network configuration in ifrefpoll files.
It inherits from OSCARS::Method.
This method is inefficient and uses ESnet-specific ifrefpoll files.
Everything in determining the network configuration, except for getting the 
list of router names, will be replaced by SNMP queries.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use File::Basename;
use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soap_method:  Update information in router, interface, and ipaddrs
#               tables
#
sub soap_method {
    my( $self ) = @_;

    my( @file_list, $router_info );

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to update routers");
    }
    chdir($self->{params}->{directory});
    opendir(DATADIR, ".") or die "Directory: $self->{params}->{directory}: $!";
    # For now, assumes all files are data files
    @file_list = grep { $_ ne '.' and $_ ne '..' } readdir(DATADIR);
    closedir(DATADIR);
    $router_info = $self->read_snmp_files(@file_list);
    $self->update_db($router_info);
    return $router_info;
} #___________________________________________________________________________


##############################################################################
# read_snmp_files:  reads list of SNMP output files
#
# In:   list of file names
#
sub read_snmp_files {
    my( $self, @file_list ) = @_;

    my( %routers, $router_name, $router_info, $interface_info );
    my( $path, $suffix );

    my @suffixlist = (".out");
    for my $fname (@file_list) {
        $interface_info = $self->read_snmp_data($fname);
        ($router_name, $path, $suffix) = fileparse($fname,@suffixlist);
        $routers{$router_name} = $interface_info;
    }
    return( \%routers );
} #___________________________________________________________________________


##############################################################################
# read_snmp_data:  Reads data for one snmp output file.
#
# It assumes snmp output in a particular order, for example
#  time              var                   data
# ...
# 1117523019     ifDescr.116                lo0.0
# ...
# 1117523019     ifAlias.116                snv2-sdn1::loopback:show:na
# ...
# 1117523019     ifSpeed.116                0
# ...
# 1117523019     ifHighSpeed.116            0
#
# 1117523019     ipAdEntIfIndex.127.0.0.1   116
#
sub read_snmp_data {
    my( $self, $fname ) = @_;

    my( %interface_info, $snmp_time, $snmp_var, $snmp_data );
    my( @snmp_fields, $ipaddr, $section_info ); 
    my $current_section = '';

    my %ipInfo = ();
    my %ifInfo = ();
    open SNMP_OUT, $fname or die("Unable to open: $!\n");
    while(<SNMP_OUT>) {
        chomp;
        ( $snmp_time, $snmp_var, $snmp_data ) = split;
        if (!$snmp_var) { next; }
        @snmp_fields = split('\.', $snmp_var);
        if ($snmp_fields[0] ne $current_section) {
            if ($snmp_fields[0] eq 'sysUpTimeInstance') {
                if ($current_section) { last; }
                else { next; }
            }
            else  {
                $current_section = $snmp_fields[0];
            }
        }
        # first part is name, second part is index
        if ($current_section ne 'ipAdEntIfIndex') {
            if ($snmp_fields[1]) {
                if (!$ifInfo{$snmp_fields[1]}) {
                    $ifInfo{$snmp_fields[1]} = { index => $snmp_fields[1] };
                }
                $ifInfo{$snmp_fields[1]}{$current_section} = $snmp_data;
            }
        }
        # second part is IP address, $snmp_data is index
        else {
            $ipaddr = join('.', @snmp_fields[1, 2, 3, 4]);
            $ipInfo{$ipaddr} = $snmp_data;
        }
    }
    close(SNMP_OUT);
    my $idx;
    for my $ip (keys(%ipInfo)) {
        $idx = $ipInfo{$ip};
        if ($ifInfo{$idx}) {
            $interface_info{$ip} = $ifInfo{$idx};
        }
        else { $interface_info{$ip} = undef; }
    }
    return( \%interface_info );
} #___________________________________________________________________________


##############################################################################
# update_db:  Compares current info with routers, interfaces, and ipaddrs
#             tables.
#
# In:   db instance, and two hashes keyed by router name
# Out:  Error if any
#
sub update_db {
    my( $self, $router_info ) = @_;

    my( $router_id, $mpls_loopback, $interface, $interface_id );

    # for now (ESnet)
    my $network_id = 1;
    for my $router_name (sort keys %$router_info) {   # sort by router name
        print STDERR "$router_name\n";
        $mpls_loopback = 'NULL';
        for my $ipaddr (sort keys %{$router_info->{$router_name}}) {    # sort by IP
            if ($ipaddr =~ /134\.55\.75\.*/) {
                 $mpls_loopback = $ipaddr;
                 last;
            }
        }
        $router_id = $self->update_routers_table($network_id, $router_name,
                                                 $mpls_loopback);
        for my $ip (keys(%{$router_info->{$router_name}})) {
            $interface = $router_info->{$router_name}->{$ip};
            if ($interface) {
                $interface_id = $self->update_xfaces_table($router_id, $interface);
                if ($interface_id) {
                    $self->update_ipaddrs_table($interface_id, $ip);
                }
            }
        }
    } 
} #___________________________________________________________________________


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
                     FROM Intradomain.routers WHERE router_name = ?";
    my $row = $self->{db}->get_row($statement, $router_name);
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into Intradomain.routers VALUES ( NULL, True,
                     '$router_name', '$mpls_loopback', $network_id)";
        $unused = $self->{db}->do_query($statement);
        $router_id = $self->{db}->{dbh}->{mysql_insertid};
        return $router_id;
    }
    $router_id = $row->{router_id};
    if (!$row->{router_loopback}) { $row->router_loopback = 'NULL'; }
    if ($row->{router_loopback} ne $mpls_loopback) {
        $statement = "UPDATE Intradomain.routers SET router_loopback = ?
                      WHERE router_id = ?";
        $unused = $self->{db}->do_query($statement, $mpls_loopback, $router_id);
    }
    return $router_id;
} #___________________________________________________________________________


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

    my $statement = "SELECT interface_id, interface_snmp_id, interface_speed,
                     interface_descr, interface_alias from Intradomain.interfaces
                     WHERE router_id = ? AND interface_snmp_id = ?";
    my $row = $self->{db}->get_row($statement, $router_id,
                                       $xface->{index});

    # defaults if non-required fields not set
    if (!$xface->{ifDescr}) { $xface->{ifDescr} = 'NULL'; }
    if (!$xface->{ifAlias}) { $xface->{ifAlias} = 'NULL'; }
    if (!$xface->{ifSpeed}) { $xface->{ifSpeed} = 0; }
    if (!$xface->{ifHighSpeed}) { $xface->{ifHighSpeed} = 0; }

    # calculate bandwidth given by new data
    my $new_speed = 0;
    if ($xface->{ifSpeed}) {
        $new_speed = $xface->{ifSpeed};
    }
    if ($xface->{ifHighSpeed}) {
        if ($new_speed < ($xface->{ifHighSpeed} * 1000000)) {
            $new_speed = $xface->{ifHighSpeed} * 1000000;
        }
    }
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into Intradomain.interfaces VALUES ( NULL, True, 
                  $xface->{index}, $new_speed, '$xface->{ifDescr}',
                  '$xface->{ifAlias}', $router_id)";
        $unused = $self->{db}->do_query($statement);
        $interface_id = $self->{db}->{dbh}->{mysql_insertid};
        return $interface_id;
    }
    $interface_id = $row->{interface_id};
    $statement = "UPDATE Intradomain.interfaces SET interface_speed = ?,
                  interface_descr = ?, interface_alias = ?
                  WHERE interface_id = ?";
    $unused = $self->{db}->do_query($statement, $new_speed,
                  $xface->{ifDescr}, $xface->{ifAlias}, $interface_id);
    return $interface_id;
} #___________________________________________________________________________


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
    my $statement = "SELECT ipaddr_id, ipaddr_ip, interface_id FROM Intradomain.ipaddrs
                     WHERE ipaddr_ip = ?";
    my $row = $self->{db}->get_row($statement, $interface_ip);
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into Intradomain.ipaddrs VALUES ( NULL, '$interface_ip',
                  $interface_id)";
        $unused = $self->{db}->do_query($statement);
        $ipaddrs_id = $self->{db}->get_primary_id();
        return $ipaddrs_id;
    }
    $ipaddrs_id = $row->{ipaddrs_id};
    return $ipaddrs_id;
} #___________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
