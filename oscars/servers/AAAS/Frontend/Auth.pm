###############################################################################
package AAAS::Frontend::Auth;

# Database interactions dealing with authorization.  TODO:  convert to ROAM.
#
# Last modified:   November 28, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use Data::Dumper;

use AAAS::Frontend::Database;
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
        'login' => $self->{levs}->{user},
        'get_info' => $self->{levs}->{user},
        'get_profile' => $self->{levs}->{user},
        'set_profile' => $self->{levs}->{user},
        'view_institutions' => $self->{levs}->{user},
        'logout' => $self->{levs}->{user},
        'view_users' => $self->{levs}->{admin},
        'view_permissions' => $self->{levs}->{admin},
        'add_user_form' => $self->{levs}->{admin},
        'add_user' => $self->{levs}->{admin},
        'delete_user' => $self->{levs}->{admin},
        'create_reservation_form' => $self->{levs}->{user},
        'create_reservation' => $self->{levs}->{user},
        'cancel_reservation' => $self->{levs}->{user},
        'view_reservations' => $self->{levs}->{user},
        'view_details' => $self->{levs}->{user},
        'find_pending_reservations' => $self->{levs}->{engr},
        'find_expired_reservations' => $self->{levs}->{engr},
    };
    $self->{method_section_permissions} = {
        'get_profile' => $self->{levs}->{admin},
        'set_profile' => $self->{levs}->{admin},
        'create_reservation_form' => $self->{levs}->{engr},
        'create_reservation' => $self->{levs}->{engr},
        'cancel_reservation' => $self->{levs}->{engr},
        'view_reservations' => $self->{levs}->{engr},
        'view_details' => $self->{levs}->{engr},
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
