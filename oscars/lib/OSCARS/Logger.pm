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

July 17, 2006

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

    $self->{login} = 'undetermined';
} #____________________________________________________________________________


###############################################################################
#
sub setMethod {
    my( $self, $methodName ) = @_;

    $self->{method} = $methodName;
} #____________________________________________________________________________


###############################################################################
#
sub setUserLogin {
    my( $self, $login ) = @_;

    $self->{login} = $login;
} #____________________________________________________________________________


###############################################################################
#
sub debug {
    my( $self, $evtName, $hash ) = @_;

    my $newEvtName = $self->{method} . '.' . $evtName;
    my $logMessage = $self->sanitize($hash);
    $self->SUPER::debug($newEvtName, $logMessage);
} #____________________________________________________________________________


###############################################################################
#
sub info {
    my( $self, $evtName, $hash ) = @_;

    my $newEvtName = $self->{method} . '.' . $evtName;
    my $logMessage = $self->sanitize($hash);
    $self->SUPER::info($newEvtName, $logMessage);
} #____________________________________________________________________________


###############################################################################
#
sub warning {
    my( $self, $evtName, $hash ) = @_;

    my $newEvtName = $self->{method} . '.' . $evtName;
    my $logMessage = $self->sanitize($hash);
    $self->SUPER::warning($newEvtName, $logMessage);
} #____________________________________________________________________________


###############################################################################
#
sub fatal {
    my( $self, $evtName, $hash ) = @_;

    my $newEvtName = $self->{method} . '.' . $evtName;
    my $logMessage = $self->sanitize($hash);
    $self->SUPER::fatal($newEvtName, $logMessage);
} #____________________________________________________________________________


###############################################################################
#
sub sanitize {
    my( $self, $hash ) = @_;

    my $logMessage = {};
    for my $key ( keys %{$hash} ) {
        if ( $key eq 'password' ) { next; }
	if ( ref( $hash->{$key} ) eq 'HASH' ) {
            $logMessage->{$key} = Dumper($hash->{$key});
        }
	else { $logMessage->{$key} = $hash->{$key}; }
    }
    if (!$logMessage->{login}) {
        $logMessage->{login} = $self->{login};
    }
    return $logMessage;
} #____________________________________________________________________________


######
1;
