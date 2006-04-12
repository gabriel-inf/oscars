#==============================================================================
package OSCARS::Interdomain::Method::Forward;

=head1 NAME

OSCARS::Interdomain::Method::Forward - Forward a request to another domain.

=head1 SYNOPSIS

  use OSCARS::Interdomain::Method::Forward;

=head1 DESCRIPTION

SOAP method to forward a request to another domain (currently only
OSCARS/BRUW).

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

April 11, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
} #____________________________________________________________________________


###############################################################################
# soap_method:  Handles forwarding a request.  In process of implementing.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    $self->{logger}->info("start", $self->{params});
    my $som = $self->{params}->{client}->dispatch($self->{params});
    if (!$som) { print STDERR "call failed\n"; }
    elsif ($som->faultstring) { print STDERR "$som->faultstring\n";}
    $self->{logger}->info("finish", $self->{params});
    return $som->result;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
