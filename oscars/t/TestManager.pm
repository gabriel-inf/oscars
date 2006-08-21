# =============================================================================
package TestManager;

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

TestManager - Perl test manager.

=head1 SYNOPSIS

  use TestManager;

=head1 DESCRIPTION

Handles tests.  TODO:  Merge with Test::Harness runtests, and have just one
instance of TestManager for all tests.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 14, 2006

=cut

use vars qw($VERSION);
$VERSION = '0.1';

use XML::Simple;

use Data::Dumper;
use Error qw(:try);

use strict;

use NetLogger;

use OSCARS::PluginManager;
use OSCARS::ClientManager;
use OSCARS::Logger;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    my $paramsMgr = OSCARS::PluginManager->new('location' => 'params.xml');
    $self->{params} = $paramsMgr->getConfiguration()->{test};
    my $configFile = $ENV{HOME} . '/.oscars.xml';
    my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
    $self->{config} = $pluginMgr->getConfiguration();
    $self->{clientMgr} = OSCARS::ClientManager->new(
                                  'configuration' => $self->{config}->{client});
    $self->{logger} = OSCARS::Logger->new();
    $self->{logger}->setUserLogin('testaccount');
    $self->{logger}->set_level($NetLogger::INFO);
    $self->{logger}->open('test.log');
} #____________________________________________________________________________


###############################################################################
#
sub dispatch {
    my( $self, $methodName, $params ) = @_;

    my $methodParams = $self->{params}->{$methodName};
    if ( $params ) {
	if ( $methodParams ) {
	    for my $key ( keys %{ $methodParams } ) {
	        $params->{$key} = $methodParams->{$key};
	    }
	}
    }
    elsif ( $methodParams ) { $params = $methodParams; }
    $self->{logger}->setMethod($methodName);
    print STDERR "\nmethod: $methodName\n";
    # special case for BNL
    if ($methodName eq 'testForward') {
        print STDERR "using password\n";
        $params->{password} =
            $self->{authN}->getCredentials($params->{login}, 'password');
        $ENV{WSS_SIGN} = 'false';
    }
    else {
        # sign using user's certificate
        $ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/usercert.pem";
        $ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/userkey.pem";
        # tells WSRF::Lite to sign the message with the above cert
        $ENV{WSS_SIGN} = 'true';
    }

    # if overriding actual method called
    if ( $params->{methodName} ) { $methodName = $params->{methodName}; }

    my $info = Data::Dumper->Dump([$params], [qw(*REQUEST)]);
    $self->{logger}->info("request", { 'fields' => substr($info, 0, -1) });
    my $client = $self->{clientMgr}->getClient($methodName);
    my $method = SOAP::Data -> name($methodName)
        -> attr ( { 'xmlns' => $self->{config}->{namespace} } );
    my $request = SOAP::Data -> name($methodName . "Request" => $params );
    my $som = $client->call($method => $request);

    if ($som->faultstring) { $self->{logger}->warn( "Error", { 'fault' => $som->faultstring }); }
    else {
        $info = Data::Dumper->Dump([$som->result], [qw(*RESPONSE)]);
        $self->{logger}->info("response", { 'fields' => substr($info, 0, -1) });
    }
    if ($som->faultstring) { return( 0, undef ); }
    return( 1, $som->result );
} #____________________________________________________________________________


sub close {
    my( $self ) = @_;

} #____________________________________________________________________________

