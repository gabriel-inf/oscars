#==============================================================================
package OSCARS::Library::AAA::AuthZ;

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

OSCARS::Library::AAA::AuthZ - handles authorization.

=head1 SYNOPSIS

  use OSCARS::Library::AAA::AuthZ;

=head1 DESCRIPTION

This module handles authorization.  It queries the database to see if a user 
is authorized to perform an action.  There is one instance of this per user.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

May 9, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::Database;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    bless($self, $class);
    $self->initialize();
    return($self);
} #____________________________________________________________________________


sub initialize {
    my( $self ) = @_;

    $self->{db} = OSCARS::Database->new();
} #____________________________________________________________________________


###############################################################################
# getResourcePermissions:  Gets all permissions associated with all resources.
#
sub getResourcePermissions {
    my( $self ) = @_;

    my( $row, $resourceName, $permissionName );

    $self->{db}->connect($self->{database});
    my $statement = "SELECT resourceId, permissionId FROM resourcePermissions";
    my $results = $self->{db}->doSelect($statement);
    my $resourcePermissions = {};
    $statement = "SELECT name FROM resources WHERE id = ?";
    my $pstatement = "SELECT name FROM permissions WHERE id = ?";
    for my $perm ( @$results ) {
	$row = $self->{db}->getRow($statement, $perm->{resourceId});
	$resourceName = $row->{name};
        if ( !$resourcePermissions->{$resourceName} ) {
            $resourcePermissions->{$resourceName} = {};
        }
	$row = $self->{db}->getRow($pstatement, $perm->{permissionId});
	$permissionName = $row->{name};
        $resourcePermissions->{$resourceName}->{$permissionName} = 1;
    }
    $self->{db}->disconnect();
    return $resourcePermissions;
} #____________________________________________________________________________


###############################################################################
# getAuthorizations:  get all authorizations for the specified user.
#
sub getAuthorizations {
    my( $self, $user ) = @_;

    my( $row, $resourceName, $permissionName );

    $self->{db}->connect($self->{database});
    my $auths = {};
    my $statement = "SELECT id from users where login = ?";
    my $results = $self->{db}->getRow($statement, $user->{login});
    my $userId = $results->{id};

    $statement = "SELECT resourceId, permissionId FROM authorizations " .
                 "WHERE userId = ?";
    $results = $self->{db}->doSelect($statement, $userId);
    my $rstatement = "SELECT name FROM resources WHERE id = ?";
    my $pstatement = "SELECT name FROM permissions WHERE id = ?";
    for my $pair ( @$results ) {
	$row = $self->{db}->getRow($rstatement, $pair->{resourceId});
	$resourceName = $row->{name};
        if ( !$auths->{$resourceName} ) {
            $auths->{$resourceName} = {};
	}
	$row = $self->{db}->getRow($pstatement, $pair->{permissionId});
	$permissionName = $row->{name};
        $auths->{$resourceName}->{$permissionName} = 1;
    }
    $self->{db}->disconnect();
    return $auths;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has a specific permission on a given resource.
#
sub authorized {
    my( $self, $user, $resourceName, $permissionName ) = @_;

    if ( !$user->{authorizations}->{$resourceName} ) {
	return 0;
    }
    elsif ( !$user->{authorizations}->{$resourceName}->{$permissionName} ) {
        return 0;
    }
    return 1;
} #____________________________________________________________________________


######
1;
