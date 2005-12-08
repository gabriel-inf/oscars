###############################################################################
package Client::NavigationBar;

# Handles printing navigation bar, and indicating which page is active
#
# Last modified:  November 20, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# Initializes tabbed bar used for navigation within the OSCARS site. 
#
sub initialize {
    my( $self, $user_permission ) = @_;

    print qq{

    <navigation_bar>
    <ul id="tabnav">
      <li>
        <a style="/styleSheets/layout.css" title="Information page"
           class="active" id="get_info"
           onclick="new_section('get_info');return false;"
           href="#">Information</a>
      </li>
    };
    if ($user_permission eq 'admin') {    # TODO:  FIX auth
      print qq{
      <li>
        <a style="/styleSheets/layout.css" title="List user accounts"
           id="view_users"
           onclick="new_section('view_users');return false;"
           href="#">List Accounts</a>
      </li>
      <li>
        <a style="/styleSheets/layout.css" title="Add a new user account"
           id="add_user_form"
           onclick="new_section('add_user_form');return false;"
           href="#">Add User</a>
      </li>
      };
    }
    print qq{
      <li>
        <a style="/styleSheets/layout.css" 
           title="View and/or edit your information"
           id="get_profile"
           onclick="new_section('get_profile');return false;"
           href="#">User Profile</a>
      </li>
      <li>
        <a style="/styleSheets/layout.css" 
           title="View/Edit selected list of reservations"
           id="view_reservations"
           onclick="new_section('view_reservations');return false;"
           href="#">View/Edit Reservations</a>
      </li>
      <li>
        <a style="/styleSheets/layout.css" title="Create a new reservation"
           id="create_reservation_form"
           onclick="new_section('create_reservation_form');return false;"
           href="#">Make Reservation</a>
      </li>
      <li>
        <a style="/styleSheets/layout.css" title="Log out on click"
           id="logout"
           href="/perl/adapt.pl?method=logout">Log out</a>
      </li>
    </ul>
    </navigation_bar>
    };
} #____________________________________________________________________________ 

######
1;
