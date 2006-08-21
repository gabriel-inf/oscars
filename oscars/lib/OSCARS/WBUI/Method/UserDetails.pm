#==============================================================================
package OSCARS::WBUI::Method::UserDetails;

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

OSCARS::WBUI::Method::UserDetails - Prints user details.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserDetails;

=head1 DESCRIPTION

Prints user details.  Used by UserAddForm, UserQuery, and UserModify.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 22, 2006

=cut


use strict;

use Data::Dumper;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# output:  print user detail input fields, minus login name and password
#          fields.
#
sub output {
    my( $self, $request, $response ) = @_;

    # take care of non_required fields
    my $description =
        $response->{description} ? $response->{description} : "";
    my $emailSecondary =
        $response->{emailSecondary} ne 'NULL' ? $response->{emailSecondary} : "";
    my $phoneSecondary =
        $response->{phoneSecondary} ne 'NULL' ? $response->{phoneSecondary} : "";

    my $firstName = $response->{firstName};
    my $lastName = $response->{lastName};
    my $institution = $response->{institutionName};
    my $emailPrimary = $response->{emailPrimary};
    my $phonePrimary = $response->{phonePrimary};
    print( qq{
      <tr>
        <td>First Name</td>
        <td><input class='required' type='text' name='firstName'
             size='40' value='$firstName'></input>
        </td>
      </tr>
      <tr>
        <td>Last Name</td>
        <td><input class='required' type='text' name='lastName' 
             size='40' value='$lastName'></input>
        </td>
      </tr>
      <tr>
        <td>Organization</td>
        <td><select class='required' name='institutionName'>
      } );
      my $institutionList = $response->{institutionList};
      for my $row (@$institutionList) {
          print("<option value='$row->{name}' ");
	  if ( $row->{name} eq $institution ) {
              print( "selected='selected'" );
	  }
	  print( ">$row->{name}</option>" );
      }
      print( qq{
          </select>
        </td>
      </tr>
      <tr>
        <td valign='top'>Personal Description</td>
          <td><input class='SOAP' type='text' name='description' size='40'
	     value='$description'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Primary)</td>
        <td><input class='required' type='text' name='emailPrimary'
             size='40' value='$emailPrimary'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Secondary)</td>
        <td><input class='SOAP' type='text' name='emailSecondary' size='40'
             value='$emailSecondary'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Primary)</td>
        <td><input class='required' type='text' name='phonePrimary'
             size='40' value='$phonePrimary'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Secondary)</td>
        <td><input class='SOAP' type='text' name='phoneSecondary' size='40'
             value='$phoneSecondary'></input>
        </td>
      </tr>
    } );
} #____________________________________________________________________________


######
1;
