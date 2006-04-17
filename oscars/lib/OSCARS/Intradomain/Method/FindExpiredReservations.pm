#==============================================================================
package OSCARS::Intradomain::Method::FindExpiredReservations;

=head1 NAME

OSCARS::Intradomain::Method::FindExpiredReservations - SOAP method to find expired 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::Intradomain::Method::FindExpiredReservations;

=head1 DESCRIPTION

SOAP method to find expired OSCARS reservations.  It calls the PSS to tear down
the label switched path for expired reservations.  Inherits from 
OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Intradomain::SchedulerCommon;
use OSCARS::Intradomain::TimeConversionCommon;
use OSCARS::Intradomain::ReservationCommon;
use OSCARS::PSS::JnxLSP;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{LSP_SETUP} = 1;
    $self->{LSP_TEARDOWN} = 0;
    $self->{sched_methods} = OSCARS::Intradomain::SchedulerCommon->new(
                                                 'db' => $self->{db});
    $self->{time_methods} = OSCARS::Intradomain::TimeConversionCommon->new(
                                                 'db' => $self->{db});
    $self->{resv_methods} = OSCARS::Intradomain::ReservationCommon->new(
                                                'user' => $self->{user},
                                                'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soap_method:  find reservations that have expired, and tear
#               them down
#
sub soap_method {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage circuits");
    }
    # find reservations whose end time is before the current time and
    # thus expired
    my $reservations = 
        $self->find_expired_reservations($self->{params}->{time_interval});

    for my $resv (@$reservations) {
        $self->{sched_methods}->map_to_ips($resv);
        $resv->{lsp_status} = $self->teardown_pss($resv);
        $self->{resv_methods}->update_reservation( $resv, 'finished',
                                                   $self->{logger} );
        $self->{logger}->info("expired",
                      { 'reservation_id' =>  $resv->{reservation_id} });
    }
    my $results = {};
    $results->{list} = $reservations;
    return $results;
} #____________________________________________________________________________


###############################################################################
# generate_messages:  generate email message
#
sub generate_messages {
    my( $self, $results ) = @_;

    my $reservations = $results->{list};
    if (!@$reservations) {
        return( undef, undef );
    }
    my( @messages );
    my( $subject_line, $msg );

    for my $resv ( @$reservations ) {
        $self->{time_methods}->convert_lsp_times($resv);
        $subject_line = "Circuit tear down status for $resv->{user_login}.";
        $msg =
            "Circuit tear down for $resv->{user_login}, for reservation(s) with parameters:\n";
        $msg .= $self->{sched_methods}->reservation_lsp_stats( $resv );
        push( @messages, {'msg' => $msg, 'subject_line' => $subject_line, 'user' => $resv->{user_login} } );
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
    my $row = $self->{db}->get_row( $statement, $time_interval );
    my $timeslot = $row->{new_time};
    $statement = qq{ SELECT * FROM Intradomain.reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    return $self->{db}->do_query($statement, $status, $timeslot,
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
    $lsp_info->{logger} = $self->{logger};
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);

    $self->{logger}->info('LSP.configure',
            { 'reservation_id' => $resv_info->{reservation_id} });
    $jnxLsp->configure_lsp($self->{LSP_TEARDOWN}, $self->{logger}); 
    if ($error = $jnxLsp->get_error())  {
        return $error;
    }
    $self->{logger}->info('LSP.teardown_complete',
            { 'reservation_id' => $resv_info->{reservation_id} });
    return "";
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
