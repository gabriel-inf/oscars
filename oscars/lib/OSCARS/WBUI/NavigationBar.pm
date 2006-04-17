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

April 17, 2006

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
    my( $self, $active_tab, $authorizations ) = @_;

    my( $active_status, $op );

    print qq{
      <navigation-bar>
      <ul id="tabnav">
    };
    my $component = 'Intradomain';
    my $method = 'ListReservations';
    if ( $method eq $active_tab ) { $active_status = 'active'; }
    else { $active_status = 'inactive'; }
    print qq{
      <li>
        <a style="/styleSheets/layout.css" title="View/edit reservations"
           onclick="return new_section('component=$component;method=$method;');"
           class='$active_status' href="#">Reservations</a>
      </li>
    };
    my $method = 'CreateReservationForm';
    if ( $method eq $active_tab ) { $active_status = 'active'; }
    else { $active_status = 'inactive'; }
    print qq{
      <li>
        <a style="/styleSheets/layout.css" title="Create an OSCARS reservation"
           onclick="return new_section('component=$component;method=$method;');"
           class='$active_status' href="#">Create Reservation</a>
      </li>
    };
    $component = 'AAA';
    if ( $authorizations && $authorizations->{ManageUsers} ) {
	$method = 'ManageUsers';
	$op = 'viewUsers';
        if ( $method eq $active_tab ) { $active_status = 'active'; }
        else { $active_status = 'inactive'; }
        print qq{
          <li>
            <a style='/styleSheets/layout.css' title='Manage user accounts'
               onclick="return new_section('component=$component;method=$method;op=$op;');"
               class='$active_status' href="#">Users</a>
          </li>
        };
    }
    else {
	$method = 'UserProfile';
	$op = 'viewProfile';
        if ( $method eq $active_tab ) { $active_status = 'active'; }
        else { $active_status = 'inactive'; }
        print qq{
          <li>
            <a style="/styleSheets/layout.css" title="View/edit my profile"
               onclick="return new_section('component=$component;method=$method;op=$op;');"
               class='$active_status' href="#">User Profile</a>
          </li>
        };
    }
    if ( $authorizations && $authorizations->{ManageUsers} ) {
      $method = 'ManageResources';
      $op = 'viewResources';
      if ( $method eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Manage resources"
             onclick="return new_section('component=$component;method=$method;op=$op;');"
             class='$active_status' href="#">Resources</a>
        </li>
      };

      $method = 'ManageAuthorizations';
      if ( $method eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      $op = 'viewAuthorizations';
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Manage authorizations"
             onclick="return new_section('component=$component;method=$method;op=$op;');"
             class='$active_status' href="#">Authorizations</a>
        </li>
      };
    }
    $method = 'Info';
    if ( $method eq $active_tab ) { $active_status = 'active'; }
    else { $active_status = 'inactive'; }
    print qq{
      <li>
        <a style='/styleSheets/layout.css' title='Information page'
           onclick="return new_section('method=$method;');"
           class='$active_status' href="#">Information</a>
      </li>
    };
    print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Log out on click"
             href="/perl/adapt.pl?component=$component;method=Logout;">Log out</a>
        </li>
      </ul>
      </navigation-bar>
    };
} #____________________________________________________________________________ 

######
1;
