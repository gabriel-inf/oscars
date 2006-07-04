#==============================================================================
package OSCARS::MethodFactory;

=head1 NAME

OSCARS::MethodFactory - Factory method for all SOAP method class instantiation

=head1 SYNOPSIS

  use OSCARS::MethodFactory;

=head1 DESCRIPTION

Factory class for all SOAP method classes.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut

use strict;
use Data::Dumper;

use OSCARS::PluginManager;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
}

###############################################################################
#
sub instantiate {
    my( $self, $user, $method ) = @_;

    return $self->{pluginMgr}->usePlugin($method, $user);
} #___________________________________________________________________________ 


# vim: et ts=4 sw=4
######
1;
