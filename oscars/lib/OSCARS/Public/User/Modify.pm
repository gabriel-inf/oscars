#==============================================================================
package OSCARS::Public::User::Modify;

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

OSCARS::Public::User::Modify - Modifies a user's profile.

=head1 SYNOPSIS

  use OSCARS::Public::User::Modify;

=head1 DESCRIPTION

This is an public SOAP method.  It modifies a user's profile.  It inherits 
from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

August 9, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  SOAP method performing requested operation on a user's profile.
#     The default operation is to get the user's profile.  This method accesses
#     the users and institutions tables.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my( $statement, $response );

    # only happens if coming in from ListUsers form, which requires
    # additional authorization
    if ( $request->{selectedUser} ) {
        # check whether this person is in the database
        $statement = 'SELECT login FROM users WHERE login = ?';
        $response = $self->{db}->getRow($statement, $request->{selectedUser});
        if ( !$response ) {
            throw Error::Simple("No such user $request->{selectedUser}.");
        }
    }
    else { $request->{selectedUser} = $self->{user}->{login}; }
    # don't want to set this field in table
    $request->{login} = undef;

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $request->{passwordNewOnce} ) {
        $request->{password} = crypt( $request->{passwordNewOnce}, 'oscars');
    }

    my $statement = 'SELECT id FROM institutions WHERE name = ?';
    my $row = $self->{db}->getRow($statement, $request->{institutionName});
    $request->{institutionId} = $row->{id};

    # TODO:  FIX way to get update fields
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{db}->doSelect( $statement );

    $statement = 'UPDATE users SET ';
    for $_ (@$rows) {
        # TODO:  allow setting field to NULL where legal
        if ( $request->{$_->{Field}} ) {
            $statement .= "$_->{Field} = '$request->{$_->{Field}}', ";
            # TODO:  check that query preparation correct
            $response->{$_->{Field}} = $request->{$_->{Field}};
	}
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE login = ?';
    $self->{db}->execStatement($statement, $request->{selectedUser});
    $response->{selectedUser} = $request->{selectedUser};
    $response->{institutionName} = $request->{institutionName};
    $statement = 'SELECT name FROM institutions';
    $response->{institutionList} = $self->{db}->doSelect($statement);
    return $response;
} #____________________________________________________________________________


######
1;
