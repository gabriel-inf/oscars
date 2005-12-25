###############################################################################
package OSCARS::BSS::Method::FindExpiredReservations;

=head1 NAME

OSCARS::BSS::Method::FindExpiredReservations - SOAP method to find expired 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::FindExpiredReservations;

=head1 DESCRIPTION

SOAP method to find expired OSCARS reservations.  It calls the PSS to tear down
the label switched path for expired reservations.  Inherits from 
OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

December 25, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;
use OSCARS::BSS::SchedulerCommon;
use OSCARS::BSS::TimeConversionCommon;
use OSCARS::BSS::ReservationCommon;
use OSCARS::PSS::JnxLSP;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{sched_methods} = OSCARS::BSS::SchedulerCommon->new(
                                                 'user' => $self->{user});
    $self->{time_methods} = OSCARS::BSS::TimeConversionCommon->new(
                                                 'user' => $self->{user},
                                                 'params' => $self->{params});
    $self->{resv_methods} = OSCARS::BSS::ReservationCommon->new(
                                                'user' => $self->{user},
                                                'params' => $self->{params});
    $self->{logger}->set_recurrent(1);
} #____________________________________________________________________________


###############################################################################
# soap_method:  find reservations that have expired, and tear
#               them down
#
sub soap_method {
    my( $self ) = @_;

    my( $reservations, $status );
    my( $error_msg );

    # find reservations whose end time is before the current time and
    # thus expired
    $reservations = $self->find_expired_reservations($self->{params}->{time_interval});
    if (!@$reservations) { return $reservations; }

    for my $resv (@$reservations) {
        $self->{sched_methods}->map_to_ips($resv);
        $resv->{lsp_status} = $self->teardown_pss($resv);
        $self->{resv_methods}->update_reservation( $resv, 'finished',
                                                   $self->{logger} );
    }
    return $reservations;
} #____________________________________________________________________________


###############################################################################
# generate_messages:  generate email message
#
sub generate_messages {
    my( $self, $reservations ) = @_;

    if (!@$reservations) {
        return( undef, undef );
    }
    my( @messages );
    my( $subject_line, $msg );

    for my $resv ( @$reservations ) {
        $self->{time_methods}->convert_lsp_times($resv);
        $subject_line = "Circuit tear down status for $resv->{user_dn}.";
        $msg =
            "Circuit tear down for $resv->{user_dn}, for reservation(s) with parameters:\n";
        $msg .= $self->{sched_methods}->reservation_lsp_stats( $resv );
        push( @messages, {'msg' => $msg, 'subject_line' => $subject_line, 'user' => $resv->{user_dn} } );
    }
    return( \@messages );
} #____________________________________________________________________________


####################
# Private methods.
####################

###############################################################################
#
sub find_expired_reservations {
    my ( $self, $time_interval ) = @_;

    my $status = 'active';
    my $statement = "SELECT now() + INTERVAL ? SECOND AS new_time";
    my $row = $self->{user}->get_row( $statement, $time_interval );
    my $timeslot = $row->{new_time};
    $statement = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    return $self->{user}->do_query($statement, $status, $timeslot,
                                        'precancel' );
} #____________________________________________________________________________


###############################################################################
# teardown_pss:  format the args and call pss to teardown the configuraion 
#
sub teardown_pss {
    my ( $self, $resv_info ) = @_;

    my ( $error );

        # Create an LSP object.
    my $lsp_info = $self->{sched_methods}->map_fields($resv_info);
    $lsp_info->{configs} = $self->{resv_methods}->get_pss_configs();
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);

    $self->{logger}->write_log("Tearing down LSP...");
    $jnxLsp->configure_lsp($self->{LSP_TEARDOWN}, $resv_info, $self->{logger}); 
    if ($error = $jnxLsp->get_error())  {
        return $error;
    }
    $self->{logger}->write_log("LSP teardown complete");
    return "";
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
