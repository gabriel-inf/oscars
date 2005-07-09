package AAAS::Frontend::Auth;

# Auth.pm:  Database interactions dealing with authorization.
# Last modified: July 8, 2005
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
    my( $self, $user_dn ) = @_;

    $self->{user_levels} = $self->{dbconn}->get_user_levels($user_dn);
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
sub get_numeric_level {
    my( $self, $level_str ) = @_;

    my( @privs, $p, $numeric_level );

    $numeric_level = $self->{user_levels}{readonly};
    @privs = split(' ', $level_str);
    for $p (@privs) {
        if ($p eq 'user') {
            $numeric_level |= $self->{user_levels}{user};
        }
        elsif ($p eq 'engr') {
            $numeric_level |= $self->{user_levels}{engr};
        }
        elsif ($p eq 'admin') {
            $numeric_level |= $self->{user_levels}{admin};
        }
    }
    return( $numeric_level );
}
######

###############################################################################
#
sub verify {
    my( $self, $user_priv, $required_privs, $convert ) = @_;

    if ($convert) {
        $user_priv = $self->get_numeric_level($user_priv);
    }
    $required_privs = $self->get_numeric_level($required_privs);
    # See if user has been activated
    if ( $user_priv == $self->{user_levels}->{inactive} ) {
        throw Common::Exception("This account is not authorized or activated yet.");
    }
    # next, see if user has at least the required privileges
    elsif (!($user_priv & $required_privs)) {
        throw Common::Exception("This function requires the following privileges: " .
                     $self->get_str_level($required_privs));
    }
    return;
}
######

1;
