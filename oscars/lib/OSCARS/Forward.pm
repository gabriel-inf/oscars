#==============================================================================
package OSCARS::Forward;

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

OSCARS::Forward - Forward a request to another domain.

=head1 SYNOPSIS

  use OSCARS::Forward;

=head1 DESCRIPTION

Forward a request to another domain (currently only OSCARS/BRUW).

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::ClientManager;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

} #____________________________________________________________________________


sub forward {
    my( $self, $request, $config, $logger ) = @_;

    my $methodName;

    if (!$request->{password}) {
        $methodName = 'forward';
        $ENV{HTTPS_CERT_FILE} = "/home/oscars/.globus/usercert.pem";
        $ENV{HTTPS_KEY_FILE}  = "/home/oscars/.globus/userkey.pem";
        # tells WSRF::Lite to sign the message with the above cert
        $ENV{WSS_SIGN} = 'true';
    }
    # BNL special case, using password
    else {
        $methodName = 'testForward';
        $ENV{WSS_SIGN} = 'false';
    }

    my $clientMgr = OSCARS::ClientManager->new(
                          'configuration' => $config->{client});
    my $client = $clientMgr->getClient($methodName, $request->{nextDomain});
    if ( !$client ) {
        $logger->info("forwarding.error",
                      { 'error' => "No such domain $request->{nextDomain}" });
        return undef;
    }

    $logger->info("forwarding.start", $request );
    my $forwardRequest = {};
    for my $key (keys %{$request}) {
        $forwardRequest->{$key} = $request->{$key};
    }
    $forwardRequest->{path} = undef;
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $login = $clientMgr->getLogin($request->{nextDomain});
    my $payload = {};
    $payload->{contentType} =  $request->{method};
    $payload->{$payload->{contentType}} = $forwardRequest;
    $payload->{login} = $login;

    my $soapRequest = SOAP::Data -> name($methodName . "Request" => $payload );
    my $som = $client->call($method => $soapRequest);
    if (!$som) {
        throw Error::Simple("Unable to make forwarding SOAP call");
    }
    if ( $som->faultstring ) {
        my $msg = $som->faultstring;
        throw Error::Simple("Unable to forward: $msg");
    }
    $logger->info("forwarding.finish", $som->result );
    return( $som->result );
}


# vim: et ts=4 sw=4
######
1;
