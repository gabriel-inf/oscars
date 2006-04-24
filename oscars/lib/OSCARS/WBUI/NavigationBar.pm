#==============================================================================
package OSCARS::WBUI::NavigationBar;

=head1 NAME

OSCARS::WBUI::NavigationBar - Handles HTML output of the tabbed navigation bar.

=head1 SYNOPSIS

  use OSCARS::WBUI::NavigationBar;

=head1 DESCRIPTION

Outputs HTML for the tabbed navigation bar, and indicates which page is
active.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 19, 2006

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
# output: Outputs tabbed bar used for navigation within the OSCARS site. 
#         Some tabs are enabled by default and do not require a specific
#         authorization.
#
sub output {
    my( $self, $activeTab, $authorizations ) = @_;

    my $activeStatus;

    print qq{
      <navigation-bar>
      <ul id="tabnav">
    };
    my $method = 'ListReservations';
    if ( $method eq $activeTab ) { $activeStatus = 'active'; }
    else { $activeStatus = 'inactive'; }
    print qq{
      <li>
        <a style="/styleSheets/layout.css" title="View/edit reservations"
           onclick="return new_section('method=$method;');"
           class='$activeStatus' href="#">Reservations</a>
      </li>
    };
    my $method = 'ReservationCreateForm';
    if ( $method eq $activeTab ) { $activeStatus = 'active'; }
    else { $activeStatus = 'inactive'; }
    print qq{
      <li>
        <a style="/styleSheets/layout.css" title="Create an OSCARS reservation"
           onclick="return new_section('method=$method;');"
           class='$activeStatus' href="#">Create Reservation</a>
      </li>
    };
    if ( $authorizations && $authorizations->{ManageUsers} ) {
	$method = 'UserList';
        if ( $method eq $activeTab ) { $activeStatus = 'active'; }
        else { $activeStatus = 'inactive'; }
        print qq{
          <li>
            <a style='/styleSheets/layout.css' title='Manage user accounts'
               onclick="return new_section('method=$method;');"
               class='$activeStatus' href="#">Users</a>
          </li>
        };
    }
    else {
	$method = 'UserQuery';
        if ( $method eq $activeTab ) { $activeStatus = 'active'; }
        else { $activeStatus = 'inactive'; }
        print qq{
          <li>
            <a style="/styleSheets/layout.css" title="View/edit my profile"
               onclick="return new_section('method=$method;');"
               class='$activeStatus' href="#">User Profile</a>
          </li>
        };
    }
    if ( $authorizations && $authorizations->{ManageUsers} ) {
      $method = 'ResourceList';
      if ( $method eq $activeTab ) { $activeStatus = 'active'; }
      else { $activeStatus = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Manage resources"
             onclick="return new_section('method=$method;');"
             class='$activeStatus' href="#">Resources</a>
        </li>
      };

      $method = 'AuthorizationList';
      if ( $method eq $activeTab ) { $activeStatus = 'active'; }
      else { $activeStatus = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Manage authorizations"
             onclick="return new_section('method=$method;');"
             class='$activeStatus' href="#">Authorizations</a>
        </li>
      };
    }
    $method = 'Info';
    if ( $method eq $activeTab ) { $activeStatus = 'active'; }
    else { $activeStatus = 'inactive'; }
    print qq{
      <li>
        <a style='/styleSheets/layout.css' title='Information page'
           onclick="return new_section('method=$method;');"
           class='$activeStatus' href="#">Information</a>
      </li>
    };
    print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Log out on click"
             href="/perl/adapt.pl?method=UserLogout;">Log out</a>
        </li>
      </ul>
      </navigation-bar>
    };
} #____________________________________________________________________________


######
1;
