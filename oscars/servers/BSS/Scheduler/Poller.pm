# SOAPMethods.pm:  SOAP methods for BSS Scheduler.
#
# Note that all authentication and authorization handling is assumed
# to have been previously done by AAAS.  Use caution if running the
# BSS on a separate machine from the one running the AAAS.
#
# Last modified:  November 9, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee       (jrlee@lbl.gov)

package BSS::Scheduler::SOAPMethods;

use strict;

use Error qw(:try);
use Data::Dumper;

use Common::Exception;
use BSS::Scheduler::DBRequests;
use PSS::LSPHandler::JnxLSP;

###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;

    $self->{LSP_SETUP} = 1;
    $self->{LSP_TEARDOWN} = 0;
    $self->{db_requests} = new BSS::Scheduler::DBRequests(
                                               'dbconn' => $self->{dbconn});
    $self->{configs} = $self->{db_requests}->get_pss_configs()->[0];
}
######

##############################################################################
# find_new_reservations:  find reservations to run.  Find all the
#    reservatations in db that need to be setup and run in the next N minutes.
#
sub find_new_reservations {
    my ($self, $inref) = @_;

    my ($resvs, $status);
    my ($error_msg);

    # find reservations that need to be scheduled
    $resvs = $self->{db_requests}->find_pending_reservations(
                                                      $inref->{time_interval});
    $self->{dbconn}->get_host_info($resvs);
    $self->{dbconn}->get_engr_fields($resvs); 
    for my $r (@$resvs) {
        # call PSS to schedule LSP
        $status = setup_pss($r);
        update_reservation( $r, $status, 'active');
    }
    return "";
}
######

##############################################################################
# find_expired_reservations:  find reservations that have expired, and tear
#                             them down
#
sub find_expired_reservations {
    my ($self, $inref) = @_;

    my ($resvs, $status);

    # find reservations whose end time is before the current time and
    # thus expired
    $resvs = $self->{db_requests}->find_expired_reservations(
                                                     $inref->{time_interval});
    for my $r (@$resvs) {
        $status = teardown_pss($self->{configs}, $r);
        update_reservation( $r, $status, 'finished');
    }
    return "";
}
######

##############################################################################
# setup_pss:  format the args and call pss to do the configuration change
#
sub setup_pss {
    my( $self, $resv_info ) = @_;   

    my( $error );

    print STDERR "execing pss to schedule reservations\n";

        # Create an LSP object.
    my $lsp_info = map_fields($self->{configs}, $resv_info);
    my $jnxLsp = new PSS::LSPHandler::JnxLSP($lsp_info);

    print STDERR "Setting up LSP...\n";
    $jnxLsp->configure_lsp($self->{LSP_SETUP}, $resv_info);
    if ($error = $jnxLsp->get_error())  {
        return( $error );
    }
    print STDERR "LSP setup complete\n" ;
    return( "" );
}
######

##############################################################################
# teardown_pss:  format the args and call pss to teardown the configuraion 
#
sub teardown_pss {
    my ( $self, $resv_info ) = @_;

    my ( $error );

        # Create an LSP object.
    my $lsp_info = map_fields($self->{configs}, $resv_info);
    my $jnxLsp = new PSS::LSPHandler::JnxLSP($lsp_info);

    print STDERR "Tearing down LSP...\n" ;
    $jnxLsp->configure_lsp($self->{LSP_TEARDOWN}, $resv_info); 
    if ($error = $jnxLsp->get_error())  {
        return( $error );
    }
    print STDERR "LSP teardown complete\n" ;
    return( "" );
}
######

##############################################################################
# update_reservation: change the status of the reservervation from pending to
#                     active
#
sub update_reservation {
    my ($self, $resv, $error_msg, $status) = @_;

    my ($status, $msg);

    if ( !$error_msg ) {
        print STDERR "Changing status to $status\n";
        ($status, $msg) = $self->{dbconn}->update_status($resv, $status);
    } else {
        print STDERR "Changing status to failed\n";
        ($status, $msg) = $self->{dbconn}->update_status($resv, 'failed');
    }
}
######

##############################################################################
#
sub map_fields {
    my ( $self, $resv ) = @_;

    my ( %lsp_info );

    %lsp_info = (
      'name' => "oscars_$resv->{reservation_id}",
      'lsp_from' => $resv->{ingress_ip},
      'lsp_to' => $resv->{egress_ip},
      'bandwidth' => $resv->{reservation_bandwidth},
      'lsp_class-of-service' => $resv->{reservation_class},
      'policer_burst-size-limit' =>  $resv->{reservation_burst_limit},
      'source-address' => $resv->{source_ip},
      'destination-address' => $resv->{destination_ip},
    );
    if ($resv->{reservation_src_port} &&
        ($resv->{reservation_src_port} != 'NULL')) {
        $lsp_info{'source-port'} = $resv->{reservation_src_port};
    }
    if ($resv->{reservation_dst_port} &&
      ($resv->{reservation_dst_port} != 'NULL')) {
        $lsp_info{'destination-port'} = $resv->{reservation_dst_port};
    }
    if ($resv->{reservation_dscp} &&
      ($resv->{reservation_dscp} != 'NULL')) {
        $lsp_info{dscp} = $resv->{reservation_dscp};
    }
    if ($resv->{reservation_protocol} &&
      ($resv->{reservation_protocol} != 'NULL')) {
        $lsp_info{protocol} = $resv->{reservation_protocol};
    }
    $lsp_info{configs} = $self->{configs};
    return ( \%lsp_info );
}
######


1;

# vim: et ts=4 sw=4
