#==============================================================================
package OSCARS::WBUI::Method::UserRemove;

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

OSCARS::WBUI::Method::UserRemove - Handles managing OSCARS users.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserRemove;

=head1 DESCRIPTION

Handles managing OSCARS users.  Requires admin privileges.  From this page
an admin can view a list of users, add or delete users, and modify all
user profiles.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# makeCall:  Make call to remove a user, and then make another call to
#            retrieve the resulting user list
#
sub makeCall {
    my( $self, $params ) = @_;

    my $methodName = $self->{method};
    my $som = $self->docLiteralRequest($methodName, $params);
    my $secondSom = $self->docLiteralRequest('userList', {});
    return $secondSom;
} #___________________________________________________________________________ 


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'UserList';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:  If the caller has admin privileges print a list of 
#      all users returned by the SOAP call
#
# In:  response from SOAP call
# Out: None
#
sub outputContent {
    my ( $self, $request, $response ) = @_;

    my $msg = "Successfully read user list.";
    my $users = $response;
    my $addSubmitStr = "return submitForm(this, 'method=UserAddForm;');";
    print( qq{
      <p>Click on the user's last name to view detailed user information.</p>
      <p><input type='button' 
          onclick="$addSubmitStr" 
          value='Add User'></input></p>
      <table id='Users.Users' cellspacing='0' width='90%' class='sortable'>
        <thead><tr>
          <td>Last Name</td><td>First Name</td><td>Login Name</td>
          <td>Organization</td><td>Phone</td><td>Action</td></tr>
        </thead>
      <tbody>
    } );
    for my $row (@$users) { $self->printUser( $row ); }
    print( qq{ </tbody></table> } );
    return $msg;
} #____________________________________________________________________________


###############################################################################
# printUser:  print the information for one user
#
sub printUser {
    my( $self, $row ) = @_;

    my $profileHrefStr = "return newSection(
                    'method=UserQuery;selectedUser=$row->{login};');";
    my $deleteHrefStr = "return newSection(
                    'method=UserRemove;selectedUser=$row->{login};');";
    print qq{
    <tr>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$profileHrefStr">
	$row->{lastName}</a></td>

      <td>$row->{firstName}</td> <td>$row->{login}</td>
      <td>$row->{institutionName}</td>
      <td>$row->{phonePrimary}</td>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$deleteHrefStr">DELETE</a></td>
    </tr>
    };
} #____________________________________________________________________________


######
1;
