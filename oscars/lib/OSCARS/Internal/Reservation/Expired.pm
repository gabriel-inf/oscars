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
# soapMethod:  find reservations that have expired, and tear
#               them down
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage circuits");
    }
    # find reservations whose end time is before the current time and
    # thus expired
    my $reservations = 
        $self->findExpiredReservations($self->{params}->{timeInterval});

    for my $resv (@$reservations) {
        $self->{schedLib}->mapToIPs($resv);
        $resv->{lspStatus} = $self->teardownPSS($resv);
        $self->{resvLib}->updateReservation( $resv, 'finished',
                                                   $self->{logger} );
        $self->{logger}->info("expired", { 'id' =>  $resv->{id} });
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

    for my $resv ( @$reservations ) {
        $self->{timeLib}->convertLspTimes($resv);
        my $subject = "Circuit tear down status for $resv->{login}.";
        my $msg =
            "Circuit tear down for $resv->{login}, for reservation(s) with parameters:\n";
        $msg .= $self->{schedLib}->reservationLspStats( $resv );
        push( @messages, {'msg' => $msg, 'subject' => $subject, 'user' => $resv->{login} } );
    }
    return( \@messages );
} #____________________________________________________________________________


####################
# Private methods.
####################

###############################################################################
#
sub findExpiredReservations {
    my ( $self, $timeInterval ) = @_;

    my $status = 'active';
    my $statement = "SELECT now() + INTERVAL ? SECOND AS newTime";
    my $row = $self->{db}->getRow( $statement, $timeInterval );
    my $timeslot = $row->{newTime};
    $statement = qq{ SELECT * FROM reservations WHERE (status = ? and
                 endTime < ?) or (status = ?)};
    return $self->{db}->doQuery($statement, $status, $timeslot, 'precancel' );
} #____________________________________________________________________________


###############################################################################
# teardownPSS:  format the args and call pss to teardown the configuraion 
#
sub teardownPSS {
    my ( $self, $resv ) = @_;

    my $error;

        # Create an LSP object.
    my $lsp_info = $self->{schedLib}->mapFields($resv);
    $lsp_info->{configs} = $self->{resvLib}->getPssConfigs();
    $lsp_info->{logger} = $self->{logger};
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);

    $self->{logger}->info('LSP.configure', { 'id' => $resv->{id} });
    $jnxLsp->configure_lsp($self->{LSP_TEARDOWN}, $self->{logger}); 
    if ($error = $jnxLsp->get_error())  {
        return $error;
    }
    $self->{logger}->info('LSP.teardownComplete', { 'id' => $resv->{id} });
    return "";
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
