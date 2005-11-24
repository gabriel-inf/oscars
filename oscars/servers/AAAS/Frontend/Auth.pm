###############################################################################
package AAAS::Frontend::Auth;

# Database interactions dealing with authorization.  TODO:  convert to ROAM.
#
# Last modified:   November 22, 2005
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
    my %levs = (
        'user' => 2,
        'engr' => 4,
        'admin' => 8,
    );
    $self->{method_permissions} = {
        'login' => $levs{user},
        'get_info' => $levs{user},
        'get_profile' => $levs{user},
        'set_profile' => $levs{user},
        'logout' => $levs{user},
        'view_users' => $levs{admin},
        'add_user' => $levs{admin},
        'create_reservation_form' => $levs{user},
        'create_reservation' => $levs{user},
        'cancel_reservation' => $levs{user},
        'view_reservations' => $levs{user},
        'view_details' => $levs{user},
        'find_pending_reservations' => $levs{engr},
        'find_expired_reservations' => $levs{engr},
    };
    $self->{method_section_permissions} = {
        'get_profile' => $levs{admin},
        'set_profile' => $levs{admin},
        'create_reservation_form' => $levs{engr},
        'create_reservation' => $levs{engr},
        'cancel_reservation' => $levs{engr},
        'view_reservations' => $levs{engr},
        'view_details' => $levs{engr},
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
            if ($section_permission eq 'admin' ) {
                $params->{admin_permission} = 1;
            }
            else { $params->{engr_permission} = 1; }
        }
    }
    return 1;
} #____________________________________________________________________________


######
1;
