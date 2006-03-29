#==============================================================================
package OSCARS::Logger; 

=head1 NAME

OSCARS::Logger - Logger for OSCARS.

=head1 SYNOPSIS

  use OSCARS::Logger;

=head1 DESCRIPTION

This package is a subclass of NetLogger.  It exists primarily to sanitize
default logging messages, to prepend a method name to each event name, and
to make sure the user login name is included with each message.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 28, 2006

=cut

use strict;

use Data::Dumper;

use NetLogger;
our @ISA = qw{NetLogger};

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{user_login} = 'undetermined';
} #____________________________________________________________________________


###############################################################################
#
sub set_user_login {
    my( $self, $user_login ) = @_;

    $self->{user_login} = $user_login;
} #____________________________________________________________________________


###############################################################################
#
sub set_op {
    my( $self, $op ) = @_;

    $self->{op} = $op;
} #____________________________________________________________________________


###############################################################################
#
sub debug {
    my( $self, $evt_name, $hash ) = @_;

    my $new_evt_name = $self->{method_name} . '.';
    if ($self->{op}) { $new_evt_name .= $self->{op} . '.'; }
    $new_evt_name .= $evt_name;
    if (!$hash->{user_login}) {
	$hash->{user_login} = $self->{user_login};
    }
    if ($hash->{user_password}) {
	$hash->{user_password} = '';
    }
    $self->SUPER::debug($new_evt_name, $hash);
} #____________________________________________________________________________


###############################################################################
#
sub info {
    my( $self, $evt_name, $hash ) = @_;

    my $new_evt_name = $self->{method_name} . '.';
    if ($self->{op}) { $new_evt_name .= $self->{op} . '.'; }
    $new_evt_name .= $evt_name;
    if (!$hash->{user_login}) {
	$hash->{user_login} = $self->{user_login};
    }
    if ($hash->{user_password}) {
	$hash->{user_password} = '';
    }
    $self->SUPER::info($new_evt_name, $hash);
} #____________________________________________________________________________


###############################################################################
#
sub warning {
    my( $self, $evt_name, $hash ) = @_;

    my $new_evt_name = $self->{method_name} . '.';
    if ($self->{op}) { $new_evt_name .= $self->{op} . '.'; }
    $new_evt_name .= $evt_name;
    if (!$hash->{user_login}) {
	$hash->{user_login} = $self->{user_login};
    }
    if ($hash->{user_password}) {
	$hash->{user_password} = '';
    }
    $self->SUPER::warning($new_evt_name, $hash);
} #____________________________________________________________________________


###############################################################################
#
sub fatal {
    my( $self, $evt_name, $hash ) = @_;

    my $new_evt_name = $self->{method_name} . '.';
    if ($self->{op}) { $new_evt_name .= $self->{op} . '.'; }
    $new_evt_name .= $evt_name;
    if (!$hash->{user_login}) {
	$hash->{user_login} = $self->{user_login};
    }
    if ($hash->{user_password}) {
	$hash->{user_password} = '';
    }
    $self->SUPER::fatal($new_evt_name, $hash);
} #____________________________________________________________________________


######
1;
