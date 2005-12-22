###############################################################################
package OSCARS::BSS::Method::CancelReservation;

=head1 NAME

OSCARS::BSS::Method::CancelReservation - SOAP method to cancel an OSCARS 
reservation.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::CancelReservation;

=head1 DESCRIPTION

SOAP method to cancel an OSCARS reservation.  Inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

December 21, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;
use OSCARS::BSS::ReservationCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{resv_methods} = OSCARS::BSS::ReservationCommon->new(
                                                     'user' => $self->{user});
} #____________________________________________________________________________


###############################################################################
# soap_method:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub soap_method {
    my( $self ) = @_;

    my $status =  $self->{resv_methods}->update_status( 'precancel' );
    return $self->{resv_methods}->view_details();
} #____________________________________________________________________________


###############################################################################
# generate_messages:  generate email message
#
sub generate_messages {
    my( $self, $resv ) = @_;

    my( @messages );
    my $user_dn = $self->{user}->{dn};
    my $msg = "Reservation cancelled by $user_dn with parameters:\n";
    $msg .= $self->{resv_methods}->reservation_stats($resv);
    my $subject_line = "Reservation cancelled by $user_dn.";
    push(@messages, { 'msg' => $msg, 'subject_line' => $subject_line, 'user' => $user_dn } ); 
    return( \@messages );
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
