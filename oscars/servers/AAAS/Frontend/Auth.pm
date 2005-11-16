package AAAS::Frontend::Auth;

# Database interactions dealing with authorization.  TODO:  needs to be 
# reimplemented.                 
#
# Last modified:  November 15, 2005
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
}
######

###############################################################################
#
sub authorized {
    my( $self, $user_dn, $resource ) = @_;

    # TODO: implement authorization through incorporating ROAM
    return 1;
}
######

1;
