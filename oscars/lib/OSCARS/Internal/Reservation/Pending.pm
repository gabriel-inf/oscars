#==============================================================================
package OSCARS::Intradomain::Method::FindPendingReservations;

=head1 NAME

OSCARS::Intradomain::Method::FindPendingReservations - SOAP method to find pending 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::Intradomain::Method::FindPendingReservations;

=head1 DESCRIPTION

SOAP method to find pending OSCARS reservations.  It calls the PSS to setup 
a label-switched path for pending reservations.  Inherits from OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

April 18, 2006

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
    $self->{schedLib} = OSCARS::Intradomain::SchedulerCommon->new(
                            'db' => $self->{db});
    $self->{timeLib} = OSCARS::Intradomain::TimeConversionCommon->new(
                            'db' => $self->{db});
    $self->{resvLib} = OSCARS::Intradomain::ReservationCommon->new(
                            'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  find reservations to run.  Find all the
#    reservations in db that need to be setup and run in the next N minutes.
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage circuits");
    }
    # find reservations that need to be scheduled
    my $reservations =
        $self->findPendingReservations($self->{params}->{pollTime});
    for my $resv (@$reservations) {
        $self->{schedLib}->mapToIPS($resv);
        # call PSS to schedule LSP
        $resv->{lspStatus} = $self->setupPSS($resv);
        $self->{resvLib}->updateReservation( $resv, 'active', 
                                                    $self->{logger} );
        $self->{logger}->info('scheduling', { 'id' =>  $resv->{id} });
    }
    my $results = {};
    $results->{list} = $reservations;
    return $results;
} #____________________________________________________________________________


###############################################################################
# generateMessages:  generate email message
#
sub generateMessages {
    my( $self, $results ) = @_;

    my $reservations = $results->{list};
    if (!@$reservations) {
        return( undef, undef );
    }
    my( @messages );
    my( $subject, $msg );

    for my $resv ( @$reservations ) {
        $self->{timeLib}->convertLspTimes($resv);
        $subject = "Circuit set up status for $resv->{login}.";
        $msg =
          "Circuit set up for $resv->{login}, for reservation(s) with parameters:\n";
            # TODO:  if more than one reservation, fix duplicated effort
        $msg .= $self->{schedLib}->reservationLspStats( $resv );
        push( @messages, {'msg' => $msg, 'subject' => $subject, 'user' => $resv->{login} } );
    }
    return( \@messages );
} #____________________________________________________________________________


#####################
# Private methods.
#####################

###############################################################################
#
sub findPendingReservations  { 
    my ( $self, $timeInterval ) = @_;

    my $status = 'pending';
    my $statement = "SELECT now() + INTERVAL ? SECOND AS newTime";
    my $row = $self->{db}->getRow( $statement, $timeInterval );
    my $timeslot = $row->{newTime};
    $statement = qq{ SELECT * FROM reservations WHERE status = ? and
                 startTime < ?};
    return $self->{db}->doQuery($statement, $status, $timeslot);
} #____________________________________________________________________________


###############################################################################
# setupPSS:  format the args and call pss to do the configuration change
#
sub setupPSS {
    my( $self, $resv ) = @_;   

    my( $error );

    $self->{logger}->info('LSP.configure', { 'id' => $resv->{id} });
    # Create an LSP object.
    my $lsp_info = $self->{schedLib}->mapFields($resv);
    $lsp_info->{configs} = $self->{resvLib}->getPssConfigs();
    $lsp_info->{logger} = $self->{logger};
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);
    $jnxLsp->configure_lsp($self->{LSP_SETUP}, $self->{logger});

    if ($error = $jnxLsp->get_error())  { return $error; }
    $self->{logger}->info('LSP.setupComplete', { 'id' => $resv->{id} });
    return "";
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
