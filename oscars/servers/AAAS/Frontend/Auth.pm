package AAAS::Frontend::Auth;

# Database interactions dealing with authorization.  TODO:  needs to be 
# reimplemented.                 
#
# Last modified:  November 17, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Error qw(:try);
use Data::Dumper;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    # These data structures are purely temporary, until ROAM incorporated

    # Permissions required to access methods.  Second permission in list
    # is required to access other user's information and reservations
    $self->{method_permissions} = {
     # AAAS
        'set_profile' => ( 'user', 'admin' ),
        'get_profile' => ( 'user', 'admin' ),
        'add_user' => ( 'admin', 'admin' ),
        'get_user_list' => ( 'admin', 'admin' ),
    # BSS
        'create_reservation' => ( 'user', 'engr' ),
        'delete_reservation' => ( 'user', 'engr' ),
        'get_user_reservations' => ( 'user', 'engr' ),
        'get_reservation_details' => ( 'user', 'engr' ),
        'get_all_reservations' => ( 'engr', 'engr' ),
        'find_pending_reservations' => ( 'engr', 'engr' ),
        'find_expired_reservations' => ( 'engr', 'engr' ),
    };

    # List of each user's permissions
    $self->{user_permissions} = {
        'dwrobertson@lbl.gov' => 'engr admin user',
        'chin@es.net' => 'engr admin user',
        'mrthompson@lbl.gov' => 'engr admin user',
        # scheduler pseudo-user
        'SCHEDULER' => 'engr',
    };
}
######

###############################################################################
# TODO: implement authorization through incorporating ROAM
#
sub authorized {
    my( $self, $user_dn, $method_name ) = @_;

    my @permissions_required = $self->{method_permissions}->{$method_name};
    my $permissions_granted = $self->{user_permissions}->{$user_dn};
    return 1;
}
######

1;
