# =============================================================================
package OSCARS::User;

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

OSCARS::User - Handles user db connections, history, authorizations.

=head1 SYNOPSIS

  use OSCARS::User;

=head1 DESCRIPTION

This module contains information about one user currently logged in.
It caches authorization information about that user, retrieved from the 
database used by the authorization plugin.  It also caches connection handles
to any other database used.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 24, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::PluginManager;
use OSCARS::Database;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    # initially not authenticated
    $self->{isAuthenticated} = 0;
    $self->{handles} = {};
    $self->{authZ} = $self->{pluginMgr}->usePlugin('authorization');
    if ( !$self->{authZ} ) {
        die( "Unable to find authorization plugin; does config file exist?");
    }
    $self->{authorizations} = $self->{authZ}->getAuthorizations($self);
} #____________________________________________________________________________


###############################################################################
# getDbHandle:  retrieves DB handle from cache for database selected, sets up 
#               connection if none exists
#
sub getDbHandle {
    my( $self, $dbname ) = @_;

    if ( !$self->{handles}->{$dbname} ) {
        $self->{handles}->{$dbname} = OSCARS::Database->new();
        $self->{handles}->{$dbname}->connect($dbname);
    }
    return $self->{handles}->{$dbname};
} #____________________________________________________________________________


###############################################################################
# closeHandles:  close all cached connections to databases
#
sub closeHandles {
    my( $self ) = @_;

    for my $db (keys(%{$self->{handles}})) {
        $self->{handles}->{$db}->disconnect();
    }
} #____________________________________________________________________________


###############################################################################
# authenticated:  returns whether user has been authenticated or not
#
sub authenticated {
    my( $self ) = @_;

    return $self->{isAuthenticated};
} #____________________________________________________________________________


###############################################################################
# setAuthenticated:  set's user authentication status
#
sub setAuthenticated {
    my( $self, $authStatus ) = @_;

    $self->{isAuthenticated} = $authStatus;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has permission to use a given resource.
#
sub authorized {
    my( $self, $resourceName, $permissionName ) = @_;

    return $self->{authZ}->authorized($self, $resourceName, $permissionName);
} #____________________________________________________________________________


###############################################################################
# getAuthorizations:  returns user's cached authorizations.
#
sub getAuthorizations {
    my( $self ) = @_;

    return $self->{authorizations};
} #____________________________________________________________________________


######
1;
