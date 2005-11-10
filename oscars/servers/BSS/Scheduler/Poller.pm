# Poller:  Polls the db looking for reservations that need to be activated
#          through the set up of LSP's, and those that have expired and
#          need the associated LSP torn down.
# Last modified:  November 6, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee (jrlee@lbl.gov)

package BSS::Scheduler::Poller;

use Data::Dumper;
use Error qw(:try);

use Common::Exception;

    # Chins PSS module to configure the routers
use PSS::LSPHandler::JnxLSP;

use BSS::Scheduler::DBRequests;

use strict;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(scheduler);


################## CONSTANTS ##############################

use constant _LSP_SETUP => 1;
use constant _LSP_TEARDOWN => 0;

my $front_end;

##############################################################################
# scheduler: Loop forever checking the DB every N minutes for reserversations
#
sub scheduler {

    print STDERR "Scheduler running\n";
    $front_end = BSS::Scheduler::DBRequests->new();

    my ($db_poll_time, $time_interval) =
         $front_end->get_time_intervals();
    while (1) {
        try {
            # find reservations that need to be actived
            find_new_reservations($time_interval);
            # find reservations that need to be deactivated 
            find_expired_reservations($time_interval);

            # check every do_poll_time seconds
            sleep($db_poll_time);
        };
        catch Common::Exception with {
            my $E = shift;
            print STDERR $E->{-text};
        };
    }
}
######

##############################################################################
# find_new_reservations:  find reservations to run.  Find all the
#    reservatations in db that need to be setup and run in the next N minutes.
#
sub find_new_reservations {
    my ($time_interval) = @_;

    my ($resvs, $status);
    my ($error_msg);
    my $pss_configs = $front_end->{dbconn}->get_pss_configs();

    # find reservations that need to be scheduled
    $resvs = $front_end->find_pending_reservations('pending', $time_interval);
    $front_end->{dbconn}->get_host_info($resvs);
    $front_end->{dbconn}->get_engr_fields($resvs); 
    for my $r (@$resvs) {
        # call PSS to schedule LSP
        $status = setup_pss($pss_configs, $r);
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
    my ($time_interval) = @_;

    my ($resvs, $status);

    # find reservations whose end time is before the current time and
    # thus expired
    $resvs = $front_end->find_expired_reservations('active', $time_interval);
    # overkill for now
    my $pss_configs = $front_end->{dbconn}->get_pss_configs();
    for my $r (@$resvs) {
        $status = teardown_pss($pss_configs, $r);
        update_reservation( $r, $status, 'finished');
    }
    return "";
}
######

##############################################################################
# setup_pss:  format the args and call pss to do the configuration change
#
sub setup_pss {
    my( $pss_configs, $resv_info ) = @_;   

    my( $error );

    print STDERR "execing pss to schedule reservations\n";

        # Create an LSP object.
    my $lsp_info = map_fields($pss_configs, $resv_info);
    my $jnxLsp = new PSS::LSPHandler::JnxLSP($lsp_info);

    print STDERR "Setting up LSP...\n";
    $jnxLsp->configure_lsp(_LSP_SETUP, $resv_info);
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
    my ( $pss_configs, $resv_info ) = @_;

    my ( $error );

        # Create an LSP object.
    my $lsp_info = map_fields($pss_configs, $resv_info);
    my $jnxLsp = new PSS::LSPHandler::JnxLSP($lsp_info);

    print STDERR "Tearing down LSP...\n" ;
    $jnxLsp->configure_lsp(_LSP_TEARDOWN, $resv_info); 
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
    my ($resv, $error_msg, $status) = @_;

    my ($status, $msg);

    if ( !$error_msg ) {
        print STDERR "Changing status to $status\n";
        ($status, $msg) = $front_end->{dbconn}->update_status($resv, $status);
    } else {
        print STDERR "Changing status to failed\n";
        ($status, $msg) = $front_end->{dbconn}->update_status($resv, 'failed');
    }
}
######

##############################################################################
#
sub map_fields {
    my ( $pss_configs, $resv ) = @_;

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
    $lsp_info{configs} = $pss_configs;
    return ( \%lsp_info );
}
######


1;

# vim: et ts=4 sw=4
