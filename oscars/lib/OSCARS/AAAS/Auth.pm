###############################################################################
package OSCARS::AAAS::Auth;

# Database interactions dealing with authorization.  TODO:  convert to ROAM.
#
# Last modified:   December 7, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use Data::Dumper;

use OSCARS::AAAS::Database;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $_self into designated class.
    bless($self, $class);
    $self->initialize();
    return($self);
}


sub initialize {
    my( $self ) = @_;

    # TODO:  replace hashes with db calls, ROAM
    $self->{levs} = {
        'user' => 2,
        'engr' => 4,
        'admin' => 8,
    };
    $self->{method_permissions} = {
        'Login' => $self->{levs}->{user},
        'GetInfo' => $self->{levs}->{user},
        'GetProfile' => $self->{levs}->{user},
        'SetProfile' => $self->{levs}->{user},
        'view_institutions' => $self->{levs}->{user},
        'Logout' => $self->{levs}->{user},
        'ViewUsers' => $self->{levs}->{admin},
        'view_permissions' => $self->{levs}->{admin},
        'AddUserForm' => $self->{levs}->{admin},
        'AddUser' => $self->{levs}->{admin},
        'DeleteUser' => $self->{levs}->{admin},
        'CreateReservationForm' => $self->{levs}->{user},
        'CreateReservation' => $self->{levs}->{user},
        'CancelReservation' => $self->{levs}->{user},
        'ViewReservations' => $self->{levs}->{user},
        'ViewDetails' => $self->{levs}->{user},
        'find_pending_reservations' => $self->{levs}->{engr},
        'find_expired_reservations' => $self->{levs}->{engr},
    };
    $self->{method_section_permissions} = {
        'GetProfile' => $self->{levs}->{admin},
        'SetProfile' => $self->{levs}->{admin},
        'CreateReservationForm' => $self->{levs}->{engr},
        'CreateReservation' => $self->{levs}->{engr},
        'CancelReservation' => $self->{levs}->{engr},
        'ViewReservations' => $self->{levs}->{engr},
        'ViewDetails' => $self->{levs}->{engr},
    };
} #____________________________________________________________________________


###############################################################################
#
sub authorized {
    my( $self, $params, $method_name ) = @_;

    if ( !($params->{user_level} & 
           $self->{method_permissions}->{$method_name}) ) {
        return 0;
    }
    my $section_permission =
                          $self->{method_section_permissions}->{$method_name};
    if ($section_permission) {
        if ( $params->{user_level} &  $section_permission ) {
            if ($section_permission == $self->{levs}->{engr} ) {
                $params->{engr_permission} = 1;
            }
            else { $params->{admin_permission} = 1; }
        }
    }
    return 1;
} #____________________________________________________________________________


######
1;
