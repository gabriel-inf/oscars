#==============================================================================
package OSCARS::WBUI::Method::UserDetails;

=head1 NAME

OSCARS::WBUI::Method::UserDetails - Prints user details.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserDetails;

=head1 DESCRIPTION

Prints user details.  Used by UserAddForm, UserQuery, and UserModify.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 26, 2006

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
    my( $self, $results ) = @_;

    # take care of non_required fields
    my $description =
        $results->{description} ? $results->{description} : "";
    my $emailSecondary =
        $results->{emailSecondary} ne 'NULL' ? $results->{emailSecondary} : "";
    my $phoneSecondary =
        $results->{phoneSecondary} ne 'NULL' ? $results->{phoneSecondary} : "";

    my $firstName = $results->{firstName};
    my $lastName = $results->{lastName};
    my $institution = $results->{institutionName};
    my $emailPrimary = $results->{emailPrimary};
    my $phonePrimary = $results->{phonePrimary};
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
      my $institutionList = $results->{institutionList};
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
