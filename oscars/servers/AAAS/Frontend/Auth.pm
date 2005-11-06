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

# Get user levels
sub get_user_levels {
    my( $self ) = @_;

    # TODO:  FIX, get_user_levels done differently
    $self->{user_levels} = $self->{dbconn}->get_user_levels();
    return;
}

###############################################################################
#
sub get_str_level {
    my( $self, $level_flag ) = @_;

    my $level = "";
    $level_flag += 0;
    if ($self->{user_levels}->{admin} & $level_flag) {
        $level .= 'admin ';
    }
    if ($self->{user_levels}->{engr} & $level_flag) {
        $level .= 'engr ';
    }
    if ($self->{user_levels}->{user} & $level_flag) {
        $level .= 'user';
    }
    return( $level );
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
