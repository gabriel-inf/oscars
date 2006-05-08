###############################################################################
package OSCARS::Internal::Routing::Update;

=head1 NAME

OSCARS::Internal::Routing::Update - SOAP method updating router-associated tables.

=head1 SYNOPSIS

  use OSCARS::Internal::Routing::Update;

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

May 4, 2006

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
        $mplsLoopback = 'NULL';
        for my $ipaddr (sort keys %{$routerInfo->{$routerName}}) {    # sort by IP
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

    my( $routerId, $unused );

    my $statement = "SELECT id, name, loopback FROM routers WHERE name = ?";
    my $row = $self->{db}->getRow($statement, $routerName);
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into routers VALUES ( NULL, True,
                     '$routerName', '$mplsLoopback')";
        $self->{db}->execStatement($statement);
        $routerId = $self->{db}->{dbh}->{mysql_insertid};
        return $routerId;
    }
    $routerId = $row->{id};
    if (!$row->{loopback}) { $row->loopback = 'NULL'; }
    if ($row->{loopback} ne $mplsLoopback) {
        $statement = "UPDATE routers SET loopback = ? WHERE id = ?";
        $self->{db}->execStatement($statement, $mplsLoopback, $routerId);
    }
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

    my( $interfaceId, $unused );

    my $statement = "SELECT id, snmpId, speed, description, alias " .
                    "from interfaces WHERE routerId = ? AND snmpId = ?";
    my $row = $self->{db}->getRow($statement, $routerId, $xface->{index});

    # defaults if non-required fields not set
    if (!$xface->{ifDescr}) { $xface->{ifDescr} = 'NULL'; }
    if (!$xface->{ifAlias}) { $xface->{ifAlias} = 'NULL'; }
    if (!$xface->{ifSpeed}) { $xface->{ifSpeed} = 0; }
    if (!$xface->{ifHighSpeed}) { $xface->{ifHighSpeed} = 0; }

    # calculate bandwidth given by new data
    my $newSpeed = 0;
    if ($xface->{ifSpeed}) {
        $newSpeed = $xface->{ifSpeed};
    }
    if ($xface->{ifHighSpeed}) {
        if ($newSpeed < ($xface->{ifHighSpeed} * 1000000)) {
            $newSpeed = $xface->{ifHighSpeed} * 1000000;
        }
    }
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into interfaces VALUES ( NULL, True, 
                  $xface->{index}, $newSpeed, '$xface->{ifDescr}',
                  '$xface->{ifAlias}', $routerId)";
        $self->{db}->execStatement($statement);
        $interfaceId = $self->{db}->{dbh}->{mysql_insertid};
        return $interfaceId;
    }
    $interfaceId = $row->{interfaceId};
    $statement = "UPDATE interfaces SET speed = ?,
                  description = ?, alias = ? WHERE id = ?";
    $self->{db}->execStatement($statement, $newSpeed,
                  $xface->{ifDescr}, $xface->{ifAlias}, $interfaceId);
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
    my( $ipaddrId, $unused );

    # TODO:  handling case where interfaceId is different?
    my $statement = "SELECT id, IP, interfaceId FROM ipaddrs WHERE IP = ?";
    my $row = $self->{db}->getRow($statement, $interfaceIP);
    # no match; need to do an insert
    if ( !$row ) {
        $statement = "INSERT into ipaddrs VALUES ( NULL, '$interfaceIP',
                  $interfaceId)";
        $self->{db}->execStatement($statement);
        $ipaddrId = $self->{db}->getPrimaryId();
        return $ipaddrId;
    }
    $ipaddrId = $row->{ipaddrId};
    return $ipaddrId;
} #___________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
