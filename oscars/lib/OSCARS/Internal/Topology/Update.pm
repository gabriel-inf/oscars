###############################################################################
package OSCARS::Internal::Topology::Update;

=head1 NAME

OSCARS::Internal::Topology::Update - SOAP method updating router-associated tables.

=head1 SYNOPSIS

  use OSCARS::Internal::Topology::Update;

=head1 DESCRIPTION

SOAP method to update the routers, interfaces, and ipaddrs tables given the 
current network configuration in the ifrefpoll files.  It inherits from 
OSCARS::Method.
This method is inefficient and uses ESnet-specific ifrefpoll files.
Everything in determining the network configuration, except for getting the 
list of router names, will be replaced by SNMP queries.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

May 23, 2006

=cut


use strict;

use File::Basename;
use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  Update information in router, interface, and ipaddrs
#              tables
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to update routers");
    }
    chdir($request->{directory});
    opendir(DATADIR, ".") or die "Directory: $request->{directory}: $!";
    # For now, assumes all files are data files
    my @fileList = grep { $_ ne '.' and $_ ne '..' } readdir(DATADIR);
    closedir(DATADIR);
    my $response = $self->readSnmpFiles(@fileList);
    $self->updateDB($response);
    return $response;
} #___________________________________________________________________________


##############################################################################
# readSnmpFiles:  reads list of SNMP output files
#
# In:   list of file names
#
sub readSnmpFiles {
    my( $self, @fileList ) = @_;

    my( %routers, $routerName, $routerInfo, $interfaceInfo );
    my( $path, $suffix );

    my @suffixlist = (".out");
    for my $fname (@fileList) {
        $interfaceInfo = $self->readSnmpData($fname);
        ($routerName, $path, $suffix) = fileparse($fname,@suffixlist);
        $routers{$routerName} = $interfaceInfo;
    }
    return( \%routers );
} #___________________________________________________________________________


##############################################################################
# readSnmpData:  Reads data for one snmp output file.
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
sub readSnmpData {
    my( $self, $fname ) = @_;

    my( %interfaceInfo, $snmpTime, $snmpVar, $snmpData );
    my( @snmpFields, $ipaddr ); 
    my $currentSection = '';

    my %ipInfo = ();
    my %ifInfo = ();
    open SNMP_OUT, $fname or die("Unable to open: $!\n");
    while(<SNMP_OUT>) {
        chomp;
        ( $snmpTime, $snmpVar, $snmpData ) = split;
        if (!$snmpVar) { next; }
        @snmpFields = split('\.', $snmpVar);
        if ($snmpFields[0] ne $currentSection) {
            if ($snmpFields[0] eq 'sysUpTimeInstance') {
                if ($currentSection) { last; }
                else { next; }
            }
            else  {
                $currentSection = $snmpFields[0];
            }
        }
        # first part is name, second part is index
        if ($currentSection ne 'ipAdEntIfIndex') {
            if ($snmpFields[1]) {
                if (!$ifInfo{$snmpFields[1]}) {
                    $ifInfo{$snmpFields[1]} = { index => $snmpFields[1] };
                }
                $ifInfo{$snmpFields[1]}{$currentSection} = $snmpData;
            }
        }
        # second part is IP address, $snmpData is index
        else {
            $ipaddr = join('.', @snmpFields[1, 2, 3, 4]);
            $ipInfo{$ipaddr} = $snmpData;
        }
    }
    close(SNMP_OUT);
    my $idx;
    for my $ip (keys(%ipInfo)) {
        $idx = $ipInfo{$ip};
        if ($ifInfo{$idx}) {
            $interfaceInfo{$ip} = $ifInfo{$idx};
        }
        else { $interfaceInfo{$ip} = undef; }
    }
    return( \%interfaceInfo );
} #___________________________________________________________________________


##############################################################################
# updateDB:  Compares current info with routers, interfaces, and ipaddrs
#             tables.
#
# In:   db instance, and two hashes keyed by router name
# Out:  Error if any
#
sub updateDB {
    my( $self, $routerInfo ) = @_;

    my( $routerId, $mplsLoopback, $interface, $interfaceId );

    for my $routerName (sort keys %$routerInfo) {   # sort by router name
        print STDERR "$routerName\n";
        for my $ipaddr (sort keys %{$routerInfo->{$routerName}}) {    # sort by IP
	    $mplsLoopback = 'NULL';
            if ($ipaddr =~ /134\.55\.75\.*/) {
                 $mplsLoopback = $ipaddr;
                 last;
            }
        }
        $routerId = $self->updateRouters($routerName, $mplsLoopback);
        for my $ip (keys(%{$routerInfo->{$routerName}})) {
            $interface = $routerInfo->{$routerName}->{$ip};
            if ($interface) {
                $interfaceId = $self->updateInterfaces($routerId, $interface);
                if ($interfaceId) {
                    $self->updateIpaddrs($interfaceId, $ip);
                }
            }
        }
    } 
} #___________________________________________________________________________


##############################################################################
# updateRouters: Compares router name and loopback with routers table,
#                and does inserts and updates as necessary.
#
# In:   router name, router MPLS loopback address
# Out:  primary key, and error message, if any
#
sub updateRouters {
    my( $self, $routerName, $mplsLoopback ) = @_;

    if ($mplsLoopback ne 'NULL') { $mplsLoopback = "'$mplsLoopback'"; }
    my $statement = "INSERT into topology.routers VALUES ( NULL, True,
                    '$routerName', $mplsLoopback, NULL)";
    $self->{db}->execStatement($statement);
    my $routerId = $self->{db}->{dbh}->{mysql_insertid};
    return $routerId;
} #___________________________________________________________________________


##############################################################################
# updateInterfaces:  Compares current row with interfaces table, and does
#                       inserts and updates if necessary.
#
# In:   router id, and ref to hash containing interface fields
# Out:  primary key in interfaces, and error message, if any
#
sub updateInterfaces {
    my( $self, $routerId, $xface ) = @_;

    my( $description, $alias, $newSpeed );

    # set to NULL if non-required fields are not set
    if (!$xface->{ifDescr}) { $description = 'NULL'; }
    # otherwise, get rid of any apostrophes or double quotes originally
    # present, and quote
    else {
        $description = $xface->{ifDescr};	
	$description =~ s/'//g;
	$description =~ s/"//g;
        $description = "'$description'";
    }
    if (!$xface->{ifAlias}) { $alias = 'NULL'; }
    else {
        $alias = $xface->{ifAlias};	
	$alias =~ s/'//g;
	$alias =~ s/"//g;
	$alias = "'$alias'";
    }
    # calculate bandwidth given by new data
    if ($xface->{ifSpeed}) { $newSpeed = $xface->{ifSpeed}; }
    else { $newSpeed = 0; }

    if ($xface->{ifHighSpeed}) {
        if ($newSpeed < ($xface->{ifHighSpeed} * 1000000)) {
            $newSpeed = $xface->{ifHighSpeed} * 1000000;
        }
    }
    my $statement = "INSERT into topology.interfaces VALUES ( NULL, True, 
        $xface->{index}, $newSpeed, $description, $alias, $routerId)";
    $self->{db}->execStatement($statement);
    my $interfaceId = $self->{db}->{dbh}->{mysql_insertid};
    return $interfaceId;
} #___________________________________________________________________________


##############################################################################
# updateIpaddrs:  Compares current row with ipaddrs table, and does
#                 inserts and updates if necessary.
#
# In:   interface id, and interface IP address
# Out:  primary key in ipaddrs, and error message, if any
#
sub updateIpaddrs {
    my( $self, $interfaceId, $interfaceIP ) = @_;

    my $statement = "INSERT into topology.ipaddrs VALUES ( NULL, '$interfaceIP',
                    $interfaceId)";
    $self->{db}->execStatement($statement);
    my $ipaddrId = $self->{db}->getPrimaryId();
    return $ipaddrId;
} #___________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
