package AAAS::Frontend::Auth;

# Auth.pm: Database interactions dealing with authorization.
#          Need to be re-implemented.                 
# Last modified:  November 12, 2005
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
