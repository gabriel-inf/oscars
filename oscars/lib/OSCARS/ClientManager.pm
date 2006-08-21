# =============================================================================
package OSCARS::ClientManager;

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

OSCARS::ClientManager - SOAP client creator.

=head1 SYNOPSIS

  use OSCARS::ClientManager;

=head1 DESCRIPTION

Creates SOAP clients, using cached information from the clients table in the
oscars database.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

June 14, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

use WSRF::Lite;
use SOAP::Lite;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{configuration}->{namespace} =
                                      'http://oscars.es.net/OSCARS/Dispatcher';
} #____________________________________________________________________________


###############################################################################
# getClient:  Returns new WSRF::Lite client for given domain, with a SOAP
#             action set to the given method.
#
sub getClient {
    my( $self, $methodName, $domain ) = @_;

    if ( !$domain ) { $domain = 'default'; }
    if (!$self->{configuration}->{$domain}) {
        print STDERR "domain $domain not handled\n";
        return undef;
    }
    my $soapAction = $self->{configuration}->{namespace} . '/' . $methodName;
    my $client = WSRF::Lite
        -> uri( $self->{configuration}->{$domain}->{uri} )
        -> proxy( $self->{configuration}->{$domain}->{proxy} )
        -> on_action ( sub { return "$soapAction" } );
    return $client;
} #____________________________________________________________________________


###############################################################################
# getLogin:  Gets login name associated with other domain.  Requests are
#            forwarded as this (pseudo) user, which must be in the users
#            table in the local domain's database.
#
sub getLogin {
    my( $self, $domain ) = @_;

    if ( !$domain ) { return undef; }
    return $self->{configuration}->{$domain}->{payloadSender};
} #____________________________________________________________________________


######
1;
