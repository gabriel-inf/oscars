###############################################################################
package OSCARS::BSS::Method::FindPendingReservations;

=head1 NAME

OSCARS::BSS::Method::FindPendingReservations - SOAP method to find pending 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::FindPendingReservations;

=head1 DESCRIPTION

SOAP method to find pending OSCARS reservations.  It calls the PSS to setup 
a label-switched path for pending reservations.  Inherits from OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

December 21, 2005

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
# soap_method:  find reservations to run.  Find all the
#    reservations in db that need to be setup and run in the next N minutes.
#
sub soap_method {
    my( $self ) = @_;

    my( $reservations, $status );
    my( $error_msg );

    # find reservations that need to be scheduled
    $reservations = $self->find_pending_reservations($self->{params}->{time_interval});
    if (!@$reservations) { return $reservations; }

    for my $resv (@$reservations) {
        $self->{sched_methods}->map_to_ips($resv);
        # call PSS to schedule LSP
        $resv->{lsp_status} = $self->setup_pss($resv);
        $self->{resv_methods}->update_reservation( $resv, 'active', 
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
        $subject_line = "Circuit set up status for $resv->{user_dn}.";
        $msg =
          "Circuit set up for $resv->{user_dn}, for reservation(s) with parameters:\n";
            # TODO:  if more than one reservation, fix duplicated effort
        $msg .= $self->{sched_methods}->reservation_lsp_stats( $resv );
        push( @messages, {'msg' => $msg, 'subject_line' => $subject_line, 'user' => $resv->{user_dn} } );
    }
    return( \@messages );
} #____________________________________________________________________________


#####################
# Private methods.
#####################

###############################################################################
#
sub find_pending_reservations  { 
    my ( $self, $time_interval ) = @_;

    my $status = 'pending';
    my $statement = "SELECT now() + INTERVAL ? SECOND AS new_time";
    my $row = $self->{user}->get_row( $statement, $time_interval );
    my $timeslot = $row->{new_time};
    $statement = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    return $self->{user}->do_query($statement, $status, $timeslot);
} #____________________________________________________________________________


###############################################################################
# setup_pss:  format the args and call pss to do the configuration change
#
sub setup_pss {
    my( $self, $resv_info ) = @_;   

    my( $error );

    $self->{logger}->write_log("Creating lsp_info...");

        # Create an LSP object.
    my $lsp_info = $self->{sched_methods}->map_fields($resv_info);
    $lsp_info->{configs} = $self->{resv_methods}->get_pss_configs();
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);

    $self->{logger}->write_log("Setting up LSP...");
    $jnxLsp->configure_lsp($self->{LSP_SETUP}, $resv_info, $self->{logger});
    if ($error = $jnxLsp->get_error())  {
        return $error;
    }
    $self->{logger}->write_log("LSP setup complete");
    return "";
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
