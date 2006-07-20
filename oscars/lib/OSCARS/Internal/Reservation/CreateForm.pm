#==============================================================================
package OSCARS::Internal::Reservation::CreateForm;

=head1 NAME

OSCARS::Internal::Reservation::CreateForm - SOAP method for creation form.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::CreateForm;

=head1 DESCRIPTION

SOAP method handling the reservation creation form.  Currently it only
passes back whether the user is authorized to set the loopbacks and the
'persistent' option, which is used to decide whether to display the
corresponding rows in the form.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 16, 2006

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
# soapMethod:  Passes back whether user is authorized to set loopback fields
#              and use the 'persistent' option.
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    $logger->info("start", $request);
    my $response = {};
    if ( $self->{user}->authorized('Domains', 'manage') ) {
        $response->{loopbacksAllowed}= 1;
    }
    if ( $self->{user}->authorized('Domains', 'manage') ||
         $self->{user}->authorized('Domains', 'persistent') ) {
        $response->{persistentAllowed}= 1;
    }
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


######
1;
