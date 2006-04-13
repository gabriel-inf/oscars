#==============================================================================
package OSCARS::BSS::Method::CancelNSI;

=head1 NAME

OSCARS::BSS::Method::CancelNSI - Handles cancelling reservation.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::CancelNSI;

=head1 DESCRIPTION

SOAP method to cancel reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

April 11, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::BSS::ReservationCommon;
use OSCARS::BSS::TimeConversionCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{resv_lib} = OSCARS::BSS::ReservationCommon->new(
                                                 'user' => $self->{user});
    $self->{time_lib} = OSCARS::BSS::TimeConversionCommon->new(
                                                 'user' => $self->{user},
                                                 'logger' => $self->{logger});
} #____________________________________________________________________________


###############################################################################
# soap_method:  Handles cancellation of reservation.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    $self->{logger}->info("start", $self->{params});
    # TODO:  ensure unprivileged user can't cancel another's reservation
    my $status =  $self->{resv_lib}->update_status(
                          $self->{params}->{reservation_id}, 'precancel' );
    my $results = $self->{resv_lib}->view_details($self->{params});
    $results->{reservation_id} = $self->{params}->{reservation_id};
    $self->{time_lib}->convert_times($results);
    $self->{logger}->info("finish", $results);
    return $results;
} #____________________________________________________________________________


###############################################################################
# generate_message:  generate cancelled email message
#
sub generate_message {
    my( $self, $resv ) = @_;

    my( @messages );
    my $user_login = $self->{user}->{login};
    my $msg = "Reservation cancelled by $user_login with parameters:\n";
    $msg .= $self->{resv_lib}->reservation_stats($resv);
    my $subject_line = "Reservation cancelled by $user_login.";
    push(@messages, { 'msg' => $msg, 'subject_line' => $subject_line, 'user' => $user_login } ); 
    return( \@messages );
} #____________________________________________________________________________


###############################################################################
# next_domain_parameters:  modify parameters before sending to next domain
#
sub next_domain_parameters {
    my( $self, $params, $next_domain ) = @_;

    my $results = {};
    for my $idx (keys %{$params}) {
        $results->{$idx} = $params->{$idx};
    }
    $results->{next_domain} = $next_domain;
    $results->{ingress_router} = undef;
    $results->{egress_router} = undef;
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
