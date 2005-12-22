###############################################################################
package OSCARS::BSS::Method::ViewReservations;

=head1 NAME

OSCARS::BSS::Method::ViewReservations - SOAP method returning OSCARS 
reservation(s).

=head1 SYNOPSIS

  use OSCARS::BSS::Method::ViewReservations;

=head1 DESCRIPTION

SOAP method to read one or more reservations from the reservations table in
the BSS database.  Inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

December 21, 2005

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
    $self->{resv_methods} = OSCARS::BSS::ReservationCommon->new(
                                                     'user' => $self->{user});
    $self->{time_methods} = OSCARS::BSS::TimeConversionCommon->new(
                                                     'user' => $self->{user});
} #____________________________________________________________________________


###############################################################################
# soap_method:  get reservations from the database.  If the user has
#     engr privileges, they can view all reservations.  Otherwise they can
#     only view their own.
#
# In:  reference to hash of parameters
# Out: reference to array of hashes
#
sub soap_method {
    my( $self ) = @_;

    my( $statement, $rows );

    my $user_dn = $self->{user}->{dn};
    if ( $self->{params}->{engr_permission} ) {
        $statement = 'SELECT * FROM reservations';
    }
    else {
        $statement = "SELECT $self->{resv_methods}->{user_fields} FROM reservations" .
                     ' WHERE user_dn = ?';
    }
    $statement .= ' ORDER BY reservation_start_time';
    if ( $self->{params}->{engr_permission} ) {
        $rows = $self->{user}->do_query($statement);
    }
    else {
        $rows = $self->{user}->do_query($statement, $user_dn);
    }
    
    # get additional fields if getting reservation details and user
    # has permission
    if ( $self->{params}->{engr_permission} && $self->{params}->{reservation_id} ) { 
        $self->{resv_methods}->get_engr_fields($rows->[0]); 
    }
    for my $resv ( @$rows ) {
        $self->{time_methods}->convert_times($resv);
        $self->{resv_methods}->get_host_info($resv);
        $self->{resv_methods}->check_nulls($resv);
    }
    return $rows;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
