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

February 10, 2006

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
#
sub output {
    my( $self, $active_tab, $authorizations ) = @_;

    my( $class, $tab, $active_status );

    print qq{
      <navigation_bar>
      <ul id="tabnav">
    };
    my $server = 'BSS';
    if ( $authorizations->{ManageReservations} &&
         $authorizations->{ManageReservations}->{exec} ) {
      $tab = 'ManageReservations';
      if ( $tab eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="View/edit reservations"
             onclick="return new_section('server=$server;method=$tab;');"
             class='$active_status' href="#">Reservations</a>
        </li>
      };
    }
    if ( $authorizations->{CreateReservationForm} &&
         $authorizations->{CreateReservationForm}->{exec} ) {
      $tab = 'CreateReservationForm';
      if ( $tab eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Create an OSCARS reservation"
             onclick="return new_section('server=$server;method=$tab;');"
             class='$active_status' href="#">Create Reservation</a>
        </li>
      };
    }
    $server = 'AAAS';
    if ( $authorizations->{ManageUsers} &&
         $authorizations->{ManageUsers}->{exec} ) {
	$tab = 'ManageUsers';
        if ( $tab eq $active_tab ) { $active_status = 'active'; }
        else { $active_status = 'inactive'; }
        print qq{
          <li>
            <a style='/styleSheets/layout.css' title='Manage user accounts'
               onclick="return new_section('server=$server;method=$tab;');"
               class='$active_status' href="#">Users</a>
          </li>
        };
    }
    elsif ( $authorizations->{UserProfile} &&
            $authorizations->{UserProfile}->{exec} ) {
	$tab = 'UserProfile';
        if ( $tab eq $active_tab ) { $active_status = 'active'; }
        else { $active_status = 'inactive'; }
        print qq{
          <li>
            <a style="/styleSheets/layout.css" title="View/edit my profile"
               onclick="return new_section('server=$server;method=$tab;');"
               class='$active_status' href="#">User Profile</a>
          </li>
        };
    }
    if ( $authorizations->{ManageResources} &&
         $authorizations->{ManageResources}->{exec} ) {
      $tab = 'ManageResources';
      if ( $tab eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Manage resources"
             onclick="return new_section('server=$server;method=$tab;');"
             class='$active_status' href="#">Resources</a>
        </li>
      };
    }
    if ( $authorizations->{ManageAuthorizations} &&
         $authorizations->{ManageAuthorizations}->{exec} ) {
      $tab = 'ManageAuthorizations';
      if ( $tab eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Manage authorizations"
             onclick="return new_section('server=$server;method=$tab;');"
             class='$active_status' href="#">Authorizations</a>
        </li>
      };
    }
    if ( $authorizations->{ManageInterDomain} &&
         $authorizations->{ManageInterDomain}->{exec} ) {
      $tab = 'ManageInterDomain';
      if ( $tab eq $active_tab ) { $active_status = 'active'; }
      else { $active_status = 'inactive'; }
      print qq{
        <li>
          <a style="/styleSheets/layout.css" 
             title="Manage interdomain communications"
             onclick="return new_section('server=$server;method=$tab;');"
             class='$active_status' href="#">Interdomain</a>
        </li>
      };
    }
    $tab = 'Info';
    if ( $tab eq $active_tab ) { $active_status = 'active'; }
    else { $active_status = 'inactive'; }
    print qq{
      <li>
        <a style='/styleSheets/layout.css' title='Information page'
           onclick="return new_section('method=$tab;');"
           class='$active_status' href="#">Information</a>
      </li>
    };
    print qq{
        <li>
          <a style="/styleSheets/layout.css" title="Log out on click"
             href="/OSCARS/adapt.pl?server=$server;method=Logout">Log out</a>
        </li>
      </ul>
      </navigation_bar>
    };
} #____________________________________________________________________________ 

######
1;
