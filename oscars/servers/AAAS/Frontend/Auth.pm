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
    };
} #____________________________________________________________________________


###############################################################################
#
sub authorized {
    my( $self, $user_level, $method_name ) = @_;

    return( $user_level & $self->{method_permissions}->{$method_name} );
} #____________________________________________________________________________


######
1;
