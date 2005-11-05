# SchedulerThread.pm:  Scheduler thread that polls the db looking for 
#                      reservations that need to be scheduled
# Last modified:  November 5, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee (jrlee@lbl.gov)

package BSS::Scheduler::SchedulerThread;

use threads;
use threads::shared;

use Data::Dumper;
use Error qw(:try);

use Common::Exception;
use Common::Mail;

    # Chins PSS module to configure the routers
use PSS::LSPHandler::JnxLSP;

    # Front end to reservations database
use BSS::Frontend::Database;
use BSS::Frontend::Scheduler;

use strict;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(start_scheduler);


################## CONSTANTS ##############################

use constant _LSP_SETUP => 1;
use constant _LSP_TEARDOWN => 0;


################## GLOBALS ##############################
# settings from global configuration file 
my ($debug);
my ($dbconn);


##############################################################################
# start_scheduler:  Start up and detach a thread to do scheduling, and
#                   return control to the main prog
#
sub start_scheduler { 
    my $handler = threads->create("scheduler");
    $handler->detach();

    return 1;
}
######

##############################################################################
# scheduler: Loop forever checking the DB every N minutes for reserversations
#
sub scheduler {

    my ($front_end);

    my $db_login = 'oscars';
    my $password = 'ritazza6';

    $dbconn = BSS::Frontend::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

    print STDERR "Scheduler running\n";
    $debug = $dbconn->get_debug_level('');
    $front_end = BSS::Frontend::Scheduler->new('dbconn' => $dbconn);

    my ($db_poll_time, $time_interval) =
         $front_end->get_time_intervals();
    while (1) {
        try {
            # find reservations that need to be actived
            if ($debug) { print STDERR "before find new_reservations\n"; }
            find_new_reservations($front_end, $time_interval);
            if ($debug) { print STDERR "after find new_reservations\n"; }

            # find reservations that need to be deactivated 
            if ($debug) { print STDERR "before find_expired_reservations\n"; }
            find_expired_reservations($front_end, $time_interval);
            if ($debug) { print STDERR "after find_expired_reservations\n"; }

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
    my ($front_end, $time_interval) = @_;

    my ($resvs, $status);
    my ($error_msg);
    my( $mailer, $mail_msg );
    $mailer = Common::Mail->new();
    my $pss_configs = $dbconn->get_pss_configs();

    # find reservations that need to be scheduled
    $resvs = $front_end->find_pending_reservations('pending', $time_interval);
    $dbconn->get_host_info($resvs);
    $dbconn->get_engr_fields($resvs); 
    for my $r (@$resvs) {
        ## calls to pss to setup reservations
        $status = setup_pss($pss_configs, $r);

        if ($debug) { print STDERR "update reservation to active\n"; }
        update_reservation( $r, $status, 'active', $front_end);
        $mail_msg = $front_end->get_lsp_stats($r, $status);
        $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "LSP set up status", $mail_msg);
        $mailer->send_mail($mailer->get_webmaster(), $r->{user_dn},
                       "Your OSCARS circuit set up status", $mail_msg);

    }
    return "";
}
######

##############################################################################
# find_expired_reservations:  find reservations that have expired, and tear
#                             them down
#
sub find_expired_reservations {
    my ($front_end, $time_interval) = @_;

    my ($resvs, $status);
    my( $mailer, $mail_msg );
    $mailer = Common::Mail->new();

    # find reservations whose end time is before the current time and
    # thus expired
    $resvs = $front_end->find_expired_reservations('active', $time_interval);
    $dbconn->get_host_info($resvs);
    $dbconn->get_engr_fields($resvs); 
    # overkill for now
    my $pss_configs = $dbconn->get_pss_configs();
    for my $r (@$resvs) {
        $status = teardown_pss($pss_configs, $r);

        if ($debug) { print STDERR "update reservation to active\n"; }
        update_reservation( $r, $status, 'finished', $front_end);
        $mail_msg = $front_end->get_lsp_stats($r, $status);
        $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "LSP tear down status", $mail_msg);
        $mailer->send_mail($mailer->get_webmaster(), $r->{user_dn},
                       "Your OSCARS circuit tear down status", $mail_msg);

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
    my ($resv, $error_msg, $status, $front_end) = @_;

    my ($update_status, $update_msg);
    my( $mailer, $mail_msg );
    $mailer = Common::Mail->new();

    if ( !$error_msg ) {
        print STDERR "Changing status to $status\n";
        ($update_status, $update_msg) = $dbconn->update_reservation('SCHEDULER', $resv, $status)
    } else {
        print STDERR "Changing status to failed\n";
        ($update_status, $update_msg) = $dbconn->update_reservation('SCHEDULER', $resv, 'failed')
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
