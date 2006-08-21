#==============================================================================
package OSCARS::Library::Topology::JnxTraceroute;

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

OSCARS::Library::Topology::JnxTraceroute - Executes traceroute on Juniper routers.

=head1 SYNOPSIS

  use OSCARS::Library::Topology::JnxTraceroute;

=head1 DESCRIPTION

Executes traceroute on Juniper routers.

=head1 AUTHORS

Chin Guok (chin@es.net),
David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 19, 2006

=cut


use Data::Dumper;
use Error qw(:try);

use strict;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{configs} = $self->getConfigs();
} #____________________________________________________________________________


###############################################################################
# traceroute:  traceroute from a Juniper router.
# Input:  source, destination
# Output: <none>
#
sub traceroute
{
    my( $self, $src, $dst ) = @_;

    my( $hopInfo, $hopCount, $cmd );

    if ( !defined($src) || !defined($dst) )  {
        throw Error::Simple("Traceroute source or destination not defined.");
    }
    if ( $src eq 'default' ) { $src = $self->{configs}->{jnxSource}; }

    # Remove subnet mask if necessary.
    # e.g. 10.0.0.0/8 => 10.0.0.0
    $dst =~ s/\/\d*$//;

    # Perform the traceroute.
    $cmd = "ssh -x -a -i $self->{configs}->{jnxKey} -l " .
           "$self->{configs}->{jnxUser} $src traceroute $dst wait " .
           "$self->{configs}->{timeout} ttl " .
           "$self->{configs}->{ttl}";
    $self->{logger}->info('JnxTraceroute.ssh',
           {'command' => $cmd, 'src' => $src, 'dst' => $dst});
    if (not(open(_TRACEROUTE_, "$cmd 2>/dev/null |")))  {
        throw Error::Simple("Unable to ssh into router and perform traceroute.");
    }

    # Reset hop information.
    $hopCount = 0;
    undef($self->{rawHopData});
    undef($self->{hops});

    # Parse the results.
    while ($hopInfo = <_TRACEROUTE_>)  {
        $self->{rawHopData}[$hopCount] = $hopInfo;
        $self->{logger}->info('JnxTraceroute', { 'hop' => substr($hopInfo, 0, -1) });

        # Get the hop IP address from output, e.g.
        #  1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
        next if ($hopInfo !~ m/\((\d+\.\d+\.\d+\.\d+)\)/);
        $self->{hops}[$hopCount] = $1;
        $hopCount++;
    }
    close(_TRACEROUTE_);
    return $src;
} #____________________________________________________________________________


###############################################################################
#
sub getConfigs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM topology.configTrace where id = 1";
    my $configs = $self->{db}->getRow($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
# getRawHopData:  returns the raw traceroute data in an array.
# Input: <none>
# Output: Array of raw traceroute data, e.g.
#         1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
#
sub getRawHopData
{
    my ($self) = @_;

    return @{$self->{rawHopData}};
} #____________________________________________________________________________


##############################################################################
# getHops:  returns the IP addresses of the hops in an array.
# Input:  <none>
# Output: array of IP addresses
#
sub getHops
{
    my ($self) = @_;

    return @{$self->{hops}};
} #____________________________________________________________________________


######
1;
