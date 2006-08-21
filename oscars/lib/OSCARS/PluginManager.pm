# =============================================================================
package OSCARS::PluginManager;

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

OSCARS::PluginManager - plugin manager for OSCARS.

=head1 SYNOPSIS

  use OSCARS::PluginManager;

=head1 DESCRIPTION

Handles plugins associated with OSCARS, given an XML configuration file.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 16, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use XML::Simple;

use Data::Dumper;
use Error qw(:try);

use strict;

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

    $self->{config} = XMLin( $self->{location} );
} #____________________________________________________________________________


###############################################################################
# getConfiguration:  returns configuration.
#
sub getConfiguration {
    my( $self ) = @_;

    return $self->{config};
} #____________________________________________________________________________

###############################################################################
# usePlugin:  use given package, given a plugin name.
#
sub usePlugin {
    my( $self, $pluginName, $user ) = @_;

    my $database;

    my $plugin = $self->{config}->{plugin}->{$pluginName};
    my $packageName = 'OSCARS::' . $plugin->{location};
    if ( $plugin->{database} ) { 
        $database =
                $self->{config}->{database}->{$plugin->{database}}->{location};
    }
    else {
        $database = $self->{config}->{database}->{'system'}->{location};
    }
    my $location = $packageName . '.pm';

    $location =~ s/(::)/\//g;
    eval { require $location };
    if (!$@) {
        return $packageName->new( 'database' => $database,
                                  'pluginMgr' => $self,
                                  'user' => $user);
    }
    else { return undef; }
} #____________________________________________________________________________


######
1;
