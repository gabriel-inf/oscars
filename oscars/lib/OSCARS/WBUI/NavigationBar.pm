#==============================================================================
package OSCARS::WBUI::NavigationBar;

=head1 NAME

OSCARS::WBUI::NavigationBar - Handles HTML output of the tabbed navigation bar.

=head1 SYNOPSIS

  use OSCARS::WBUI::NavigationBar;

=head1 DESCRIPTION

Outputs initial HTML for the tabbed navigation bar.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 15, 2006

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
# init:   Outputs initial HTML for tabbed bar used for navigation within the 
#         system.  Initially, no tab is active.
#
# In:   hash indicating which tabs will be displayed (some require
#       specific authorization).
# Out:  None
sub init {
    my( $self, $tabs ) = @_;

    my $method;

    print qq{
      <navigation>
      <ul id="tabnav">
    };
    if ( $tabs->{ListReservations} ) {
      $method = 'ListReservations';
      print qq{
      <li>
        <a id="ListReservations"
           style="/styleSheets/layout.css" title="View/edit reservations"
           onclick="return newSection('method=$method;');"
           href="#">Reservations</a>
      </li>
      };
    }
    if ( $tabs->{ReservationCreateForm} ) {
      $method = 'ReservationCreateForm';
      print qq{
      <li>
        <a id="ReservationCreateForm" style="/styleSheets/layout.css" 
           title="Create an OSCARS reservation"
           onclick="return newSection('method=$method;');"
           href="#">Create Reservation</a>
      </li>
      };
    }
    # Of the following two, only one should have been indicated by the
    # SOAP server.
    if ( $tabs->{UserList} ) {
      $method = 'UserList';
      print qq{
      <li>
        <a id="UserList" style='/styleSheets/layout.css' 
           title='Manage user accounts'
           onclick="return newSection('method=$method;');"
           href="#">Users</a>
      </li>
      };
    }
    if ( $tabs->{UserQuery} ) {
      $method = 'UserQuery';
      print qq{
      <li>
        <a id="UserQuery" style="/styleSheets/layout.css" 
           title="View/edit my profile"
           onclick="return newSection('method=$method;');"
           href="#">User Profile</a>
      </li>
      };
    }
    if ( $tabs->{ResourceList} ) {
      $method = 'ResourceList';
      print qq{
      <li>
        <a id="ResourceList" style="/styleSheets/layout.css" 
           title="Manage resources"
           onclick="return newSection('method=$method;');"
           href="#">Resources</a>
      </li>
      };
    }
    if ( $tabs->{AuthorizationList} ) {
      $method = 'AuthorizationList';
      print qq{
      <li>
        <a id="AuthorizationList" style="/styleSheets/layout.css" 
           title="Manage authorizations"
           onclick="return newSection('method=$method;');"
           href="#">Authorizations</a>
      </li>
      };
    }
    print qq{
        <li>
          <a id="UserLogout" style="/styleSheets/layout.css" 
             title="Log out on click"
             href="/perl/adapt.pl?method=UserLogout;">Log out</a>
        </li>
      </ul>
      </navigation>
    };
} #____________________________________________________________________________


######
1;
