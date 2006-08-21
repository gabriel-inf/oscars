#==============================================================================
package OSCARS::Library::Topology::Router;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

=head1 NAME

OSCARS::Library::Topology::Router - Functionality for router handling 

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Router;

=head1 DESCRIPTION

Functionality dealing with router handling.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;
use vars qw(@ISA);
@ISA = qw(OSCARS::Library::Topology::Address);

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::Topology::JnxSNMP;
use OSCARS::Library::Topology::Address;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{jnxSnmp} = OSCARS::Library::Topology::JnxSNMP->new(
                                                     'db' => $self->{db} );
} #____________________________________________________________________________


###############################################################################
#
sub name {
    my( $self, $ipaddr, $ipaddrId ) = @_;
 
    my $row;

    my $statement = 'SELECT r.name FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId ' .
        'INNER JOIN topology.ipaddrs ip ON i.id = ip.interfaceId';
    if ($ipaddrId) {
        $statement .= ' WHERE ip.id = ?';
        $row = $self->{db}->getRow($statement, $ipaddrId);
    }
    else { 
        $statement .= ' WHERE ip.IP = ?';
        $row = $self->{db}->getRow($statement, $ipaddr);
    }
    return $row->{name};
} #____________________________________________________________________________


###############################################################################
# interface:  Finds whether interface associated with IP address or host
#                name.
# IN:  IP address
# OUT: associated interface
#
sub interface {
    my( $self, $hop ) = @_;

    my $ipaddr = $self->nameToIP($hop);
    my $statement = 'SELECT interfaceId FROM topology.ipaddrs WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    return $row->{interfaceId};
} #____________________________________________________________________________


###############################################################################
# info:  Gets trace or loopback IP address, if any, of router. 
# In:  IP address associated with router, and type of address to look from
# Out: alternative addres to do traceroute from, or loopback address, if any
#
sub info {
    my( $self, $ipaddr, $addressType ) = @_;

    my $routerName = $self->name($ipaddr);
    if ( !$routerName ) { return undef; }

    # given router name, get address if any
    my $statement = 'SELECT IP FROM topology.ipaddrs ip ' .
        'INNER JOIN topology.interfaces i ON i.id = ip.interfaceId ' .
        'INNER JOIN topology.routers r ON r.id = i.routerId ' .
        "WHERE r.name = ? AND ip.description = '$addressType'";
    my $row = $self->{db}->getRow($statement, $routerName);
    return( $row->{IP} );
} #____________________________________________________________________________


###############################################################################
# queryDomain:  Gets the autonomous service number associated with an IP 
#     address by performing an SNMP query against the egress router
#                
# In:  IP address of egress router, IP address of next hop
# Out: autonomous service number (used by the AAAS to look up uri and proxy)
#
sub queryDomain {
    my( $self, $interfaceIP, $ipaddr ) = @_;

    my $asNumber;

    # temporary kludge
    $interfaceIP = $self->nameToIP( $interfaceIP );
    # Get router name for logging.  If unable to get, router not in db
    my $routerName = $self->name( $interfaceIP );
    if ( !$routerName ) {
        throw Error::Simple("Pathfinder.getAsNumber: no router in database for $interfaceIP");
    }
    $self->{jnxSnmp}->initializeSession( $routerName );
    my $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        throw Error::Simple("Unable to initialize SNMP session: $errorMsg");
    }
    $asNumber = $self->{jnxSnmp}->queryAsNumber($ipaddr);
    $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        #Log SNMP failure but build reservation up to this point
        $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
              'errorMessage' => $errorMsg });
        $asNumber = 'noSuchInstance';
    }
    else {
        $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
            'AS' => $asNumber });
    }
    return $asNumber;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
