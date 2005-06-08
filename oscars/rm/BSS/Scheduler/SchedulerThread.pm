######################################################################
# Scheduler thread that polls the db looking for 
# reservataions that need to be scheduled
#
# Poll time now comes from the config file.
#
# JRLee
######################################################################
package BSS::Scheduler::SchedulerThread;

use threads;
use threads::shared;

use Data::Dumper;

    # Chins PSS module to configure the routers
use PSS::LSPHandler::JnxLSP;

    # Front end to reservations database
use BSS::Frontend::Reservation;

# try to keep it tight
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

my ($_error);

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

    my ($front_end, $error_msg);

    print STDERR "Scheduler running\n";
    $front_end = BSS::Frontend::Reservation->new('configs' => $configs);

    while (1) {

        # find reservations that need to be actived
        if ($configs->{debug}) { print STDERR "find new\n"; }
        $error_msg = find_new_reservations($front_end);
        if ($error_msg) {
            if ($configs->{debug}) { print STDERR '** ', $error_msg, "\n"; }
        }


        # find reservations that need to be deactivated 
        if ($configs->{debug}) { print STDERR "find_exp\n"; }
        $error_msg = find_expired_reservations($front_end);
        if ($error_msg) {
            if ($configs->{debug}) { print STDERR '** ', $error_msg, "\n"; }
        }

        # check every do_poll_time seconds
        sleep($configs->{db_poll_time});
    }
}
######

##############################################################################
# find_new_reservations:  find reservations to run.  Find all the
#    reservatations in db that need to be setup and run in the next N minutes.
#
sub find_new_reservations {
    my ($front_end) = @_;

    my ($timeslot, $resv, $status);
    my ($error_msg);
    my $cur_time = localtime();

    # configurable
    $timeslot = time() + $configs->{reservation_time_interval};
    if ($configs->{debug}) {
        print STDERR "pending: $cur_time \n";
    }

    # find reservations that need to be scheduled
    ($error_msg, $resv) = $front_end->find_pending_reservations($timeslot, $configs->{PENDING});
    if ($error_msg) {
        return ($error_msg);
    }

    for my $r (@$resv) {
        ## calls to pss to setup reservations
        my %lsp_info = map_fields($front_end, $r);
        $status = setup_pss(\%lsp_info, $r);
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
    my ($front_end) = @_;

    my $cur_time = localtime();
    my ($timeslot, $resv, $status, $error_msg);

    # configurable
    $timeslot = time() + $configs->{reservation_time_interval};
    if ($configs->{debug}) { print STDERR "expired: $cur_time \n"; }

    # find active reservation past the timeslot
    ($error_msg, $resv) = $front_end->find_expired_reservations($timeslot, $configs->{ACTIVE});
    if ($error_msg) { return $error_msg; }
       
    for my $r (@$resv) {
        my %lsp_info = map_fields($front_end, $r);
        $status = teardown_pss(\%lsp_info, $r);

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
    my ($lspInfo, $r) = @_;   

    my ($_error, $status);

    print STDERR "execing pss to schedule reservations\n";

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($_jnxLsp) = new PSS::LSPHandler::JnxLSP($lspInfo);

        print STDERR "Setting up LSP...\n";
        $_jnxLsp->configure_lsp(_LSP_SETUP, $r);
        if ($_error = $_jnxLsp->get_error())  {
            return( $_error );
            #die($_error);
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

    my ($_error);

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($_jnxLsp) = new PSS::LSPHandler::JnxLSP($lspInfo);

        print STDERR "Tearing down LSP...\n" ;
        $_jnxLsp->configure_lsp(_LSP_TEARDOWN, $r); 
        if ($_error = $_jnxLsp->get_error())  {
            return( $_error );
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

    if ( !$error_msg ) {
        print STDERR "Changing status to $status\n";
        $front_end->update_reservation($resv, $status)
    } else {
        print STDERR "Changing status to failed\n";
        $front_end->update_reservation($resv, $configs->{FAILED})
    }
}
######

##############################################################################
sub map_fields {
    my ( $front_end, $data ) = @_;

    my ( %results, $error );
    my ( $ingress_loopback_ip, $egress_loopback_ip, $src_hostaddrs_ip, $dst_hostaddrs_ip );

     # get loopbacks for routers, given interface ids, if an engineer
     # has not specified one  (TODO:  error checking)
    if (!(defined($data->{lsp_from}))) {
        ($ingress_loopback_ip, $error) = $front_end->{dbconn}->xface_id_to_loopback($data->{ingress_interface_id}, 'ip');
    }
    else {
        $ingress_loopback_ip = $data->{lsp_from};
    }
    if (!(defined($data->{lsp_to}))) {
        ($egress_loopback_ip, $error) = $front_end->{dbconn}->xface_id_to_loopback($data->{egress_interface_id}, 'ip');
    }
    else {
        $egress_loopback_ip = $data->{lsp_to};
    }
     print "lsp_from: $ingress_loopback_ip, lsp_to:  $egress_loopback_ip\n";
     # get host IP addresses, given id 
    ($src_hostaddrs_ip, $error) = $front_end->{dbconn}->hostaddrs_id_to_ip($data->{src_hostaddrs_id});
    ($dst_hostaddrs_ip, $error) = $front_end->{dbconn}->hostaddrs_id_to_ip($data->{dst_hostaddrs_id});
    %results = (
      'name' => "oscars_$data->{reservation_id}",
      'lsp_from' => $ingress_loopback_ip,
      'lsp_to' => $egress_loopback_ip,
      'bandwidth' => $data->{reservation_bandwidth},
      'lsp_class-of-service' => $data->{reservation_class},
      'policer_burst-size-limit' =>  $data->{reservation_burst_limit},
      'source-address' => $src_hostaddrs_ip,
      'destination-address' => $dst_hostaddrs_ip,
    );
    return ( %results );
}

1;

# vim: et ts=4 sw=4
