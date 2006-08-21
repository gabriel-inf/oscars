#==============================================================================
package OSCARS::Method;

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

OSCARS::Method - Superclass for all SOAP methods.

=head1 SYNOPSIS

  use OSCARS::Method;

=head1 DESCRIPTION

Superclass for all SOAP methods.  Contains methods for all phases of
a SOAP request.  Assumes that authentication has already been performed.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Mail;
use OSCARS::Forward;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{forwarder} = OSCARS::Forward->new();
    $self->{paramTests} = {};
    $self->{db} = $self->{user}->getDbHandle($self->{database});
    $self->{configuration} = $self->{pluginMgr}->getConfiguration();
    $self->{mailer} = OSCARS::Mail->new(
                   'configuration' => $self->{configuration}->{notification} );
} #____________________________________________________________________________


###############################################################################
# authorized:  Check whether user calling this method has the proper 
#     authorizations, including viewing and setting parameters.  If not 
#     overriden, a noop.
#
sub authorized {
    my( $self ) = @_;

    return 1;
} #____________________________________________________________________________


###############################################################################
# validate:  validate incoming parameters
#
sub validate {
    my( $self, $params ) = @_;

    my( $test );

    my $method = $params->{method};
    if ( !$method ) { return; }

    # for all tests 
    for my $testName (keys(%{$self->{paramTests}->{$method}})) {
        $test = $self->{paramTests}->{method}->{$testName};
        if (!$params->{$testName}) {
            throw Error::Simple(
                "Cannot validate $method, test $testName failed");
        }
        if ($params->{$testName} !~ $test->{regexp}) {
            throw Error::Simple( $test->{error} );
        }
    }
} #____________________________________________________________________________


###############################################################################
sub numericCompare {
    my( $self, $val, $lesser, $greater ) = @_;

    if ($lesser > $val) { return 0; }
    if ($greater < $val) { return 0; }
    return 1;
} #____________________________________________________________________________


###############################################################################
# postProcess:  Perform any operations necessary after making SOAP call
#
sub postProcess {
    my( $self, $params, $results ) = @_;

    # must be notification entry in the XML configuration file for this
    # method, for any message to be sent
    $self->{mailer}->sendMessage($self->{user}->{login}, $params->{method},
                                 $results);
} #___________________________________________________________________________ 


# vim: et ts=4 sw=4
######
1;
