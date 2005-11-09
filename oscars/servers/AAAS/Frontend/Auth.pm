package AAAS::Frontend::Auth;

# Auth.pm:  Database interactions dealing with authorization.
# Last modified: November 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;

use AAAS::Frontend::Database;
use Error qw(:try);
use Common::Exception;
use Data::Dumper;

###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
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
