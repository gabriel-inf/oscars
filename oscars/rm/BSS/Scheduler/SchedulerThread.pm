######################################################################
# July 8, 2005
# DWRobertson
# JRLee
# Scheduler thread that polls the db looking for 
# reservataions that need to be scheduled
#
# Poll time now comes from the config file.
#
######################################################################
package BSS::Scheduler::SchedulerThread;

use threads;
use threads::shared;

use Data::Dumper;
use Error qw(:try);

use Common::Mail;
use Common::Exception;

    # Chins PSS module to configure the routers
use PSS::LSPHandler::JnxLSP;

    # Front end to reservations database
use BSS::Frontend::Scheduler;
use BSS::Frontend::Stats;

use strict;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(start_scheduler);


################## CONSTANTS ##############################

use constant _LSP_SETUP => 1;
use constant _LSP_TEARDOWN => 0;


################## GLOBALS ##############################
# settings from global configuration file 
my ($configs);


#fake pss calls for the momment
my ($fakeit) = 0;

##############################################################################
# start_scheduler:  Start up and detach a thread to do scheduling, and
#                   return control to the main prog
#
sub start_scheduler { 
    # get a copy of the config
    $configs = shift;

    if ($configs->{debug}) { print STDERR "Starting Scheduler\n"; }
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

    print STDERR "Scheduler running\n";
    $front_end = BSS::Frontend::Scheduler->new('configs' => $configs);
    my $pseudo_user = 'SCHEDULER';
    try {
        $front_end->{dbconn}->login_user($pseudo_user);
    }
    catch Common::Exception with {
        my $E = shift;
        print STDERR $E->{-text};
    };

    while (1) {
        try {
            # find reservations that need to be actived
            if ($configs->{debug}) { print STDERR "before find new_reservations\n"; }
            find_new_reservations($pseudo_user, $front_end);
            if ($configs->{debug}) { print STDERR "after find new_reservations\n"; }

            # find reservations that need to be deactivated 
            if ($configs->{debug}) { print STDERR "before find_expired_reservations\n"; }
            find_expired_reservations($pseudo_user, $front_end);
            if ($configs->{debug}) { print STDERR "after find_expired_reservations\n"; }

            # check every do_poll_time seconds
            sleep($configs->{db_poll_time});
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
    my ($user_dn, $front_end) = @_;

    my ($timeslot, $resv, $status);
    my ($error_msg);
    my $cur_time = localtime();

    # configurable
    $timeslot = time() + $configs->{reservation_time_interval};
    if ($configs->{debug}) {
        print STDERR "pending: $cur_time \n";
    }

    # find reservations that need to be scheduled
    $resv = $front_end->find_pending_reservations($user_dn, $timeslot, $configs->{PENDING});
    my( $mailer, $stats, $mail_msg );
    $mailer = Common::Mail->new();
    $stats = BSS::Frontend::Stats->new();
    for my $r (@$resv) {
        ## calls to pss to setup reservations
        my %lsp_info = map_fields($front_end, $r);
        $status = setup_pss(\%lsp_info, $r);

        $mail_msg = $stats->get_lsp_stats(\%lsp_info, $r, $status);
        $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "LSP set up status", $mail_msg);

        if ($configs->{debug}) { print STDERR "update reservation to active\n"; }
        update_reservation( $r, $status, $configs->{ACTIVE}, $front_end);
    }
    return "";
}
######

##############################################################################
# find_expired_reservations:  find reservations that have expired, and tear
#                             them down
#
sub find_expired_reservations {
    my ($user_dn, $front_end) = @_;

    my $cur_time = localtime();
    my ($timeslot, $resv, $status);

    # configurable
    $timeslot = time() + $configs->{reservation_time_interval};
    if ($configs->{debug}) { print STDERR "expired: $cur_time \n"; }

    # find active reservation past the timeslot
    $resv = $front_end->find_expired_reservations($user_dn, $timeslot, $configs->{ACTIVE});
       
    my( $mailer, $stats, $mail_msg );
    $mailer = Common::Mail->new();
    $stats = BSS::Frontend::Stats->new();

    for my $r (@$resv) {
        my %lsp_info = map_fields($front_end, $r);
        $status = teardown_pss(\%lsp_info, $r);

        $mail_msg = $stats->get_lsp_stats(\%lsp_info, $r, $status);
        $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "LSP tear down status", $mail_msg);

        if ($configs->{debug}) { print STDERR "update reservation to active\n"; }
        update_reservation( $r, $status, $configs->{FINISHED}, $front_end);
    }
    return "";
}
######

##############################################################################
# setup_pss:  format the args and call pss to do the configuration change
#
sub setup_pss {
    my( $lspInfo, $r ) = @_;   

    my( $error );

    print STDERR "execing pss to schedule reservations\n";

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my $jnxLsp = new PSS::LSPHandler::JnxLSP($lspInfo);

        print STDERR "Setting up LSP...\n";
        $jnxLsp->configure_lsp(_LSP_SETUP, $r);
        if ($error = $jnxLsp->get_error())  {
            return( $error );
        }
    }
    print STDERR "LSP setup complete\n" ;
    return( "" );
}
######

##############################################################################
# teardown_pss:  format the args and call pss to teardown the configuraion 
#
sub teardown_pss {
    my ($lspInfo, $r) = @_;

    my ($error);

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($jnxLsp) = new PSS::LSPHandler::JnxLSP($lspInfo);

        print STDERR "Tearing down LSP...\n" ;
        $jnxLsp->configure_lsp(_LSP_TEARDOWN, $r); 
        if ($error = $jnxLsp->get_error())  {
            return( $error );
        }
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

    if ( !$error_msg ) {
        print STDERR "Changing status to $status\n";
        ($update_status, $update_msg) = $front_end->{dbconn}->update_reservation('SCHEDULER', $resv, $status)
    } else {
        print STDERR "Changing status to failed\n";
        ($update_status, $update_msg) = $front_end->{dbconn}->update_reservation('SCHEDULER', $resv, $configs->{FAILED})
    }
}
######

##############################################################################
sub map_fields {
    my ( $front_end, $data ) = @_;

    my ( %results, $error );
    my ( $ingress_loopback_ip, $egress_loopback_ip, $src_address, $dst_address );

     # get loopbacks for routers, given interface ids, if an engineer
     # has not specified one  (TODO:  error checking)
    if (!$data->{lsp_from}) {
        ($ingress_loopback_ip, $error) = $front_end->{dbconn}->xface_id_to_loopback('SCHEDULER', $data->{ingress_interface_id}, 'ip');
    }
    else {
        $ingress_loopback_ip = $data->{lsp_from};
    }
    if (!$data->{lsp_to}) {
        ($egress_loopback_ip, $error) = $front_end->{dbconn}->xface_id_to_loopback('SCHEDULER', $data->{egress_interface_id}, 'ip');
    }
    else {
        $egress_loopback_ip = $data->{lsp_to};
    }
     print "lsp_from: $ingress_loopback_ip, lsp_to:  $egress_loopback_ip\n";
     # get host name or IP address, given id 
    ($src_address, $error) = $front_end->{dbconn}->hostaddrs_id_to_ip('SCHEDULER', $data->{src_hostaddr_id});
    ($dst_address, $error) = $front_end->{dbconn}->hostaddrs_id_to_ip('SCHEDULER', $data->{dst_hostaddr_id});
    %results = (
      'name' => "oscars_$data->{reservation_id}",
      'lsp_from' => $ingress_loopback_ip,
      'lsp_to' => $egress_loopback_ip,
      'bandwidth' => $data->{reservation_bandwidth},
      'lsp_class-of-service' => $data->{reservation_class},
      'policer_burst-size-limit' =>  $data->{reservation_burst_limit},
      'source-address' => $src_address,
      'destination-address' => $dst_address,
    );
    if ($data->{reservation_src_port} &&
        ($data->{reservation_src_port} != 'NULL')) {
        $results{'source-port'} = $data->{reservation_src_port};
    }
    if ($data->{reservation_dst_port} &&
      ($data->{reservation_dst_port} != 'NULL')) {
        $results{'destination-port'} = $data->{reservation_dst_port};
    }
    if ($data->{reservation_dscp} &&
      ($data->{reservation_dscp} != 'NULL')) {
        $results{dscp} = $data->{reservation_dscp};
    }
    if ($data->{reservation_protocol} &&
      ($data->{reservation_protocol} != 'NULL')) {
        $results{protocol} = $data->{reservation_protocol};
    }
    return ( %results );
}

1;

# vim: et ts=4 sw=4
