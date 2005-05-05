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
use BSS::Frontend::Database;

# try to keep it tight
use strict;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(start_scheduler);


################## CONSTANTS ##############################

use constant _LSP_SETUP => 1;
use constant _LSP_TEARDOWN => 0;

#XXX: move to somewhere where everyone can use them
use constant ACTIVE =>    'active';
use constant CANCELED =>  'canceled';
use constant FAILED  =>   'failed';
use constant FINISHED =>  'finished';
use constant PENDING =>   'pending';


################## GLOBALS ##############################
# global config file 
my ($global_config);


#fake pss calls for the momment
my ($fakeit) = 0;

my ($_error);

######################################################################
# This should probably be just new or 
# some such OO thing. Just startup and deatch
# a thread to do scheduling, return control
# to the main prog
######################################################################
sub start_scheduler {

    # get a copy of the config
    $global_config = shift;

    print STDERR "Starting Scheduler\n";
    my $handler = threads->create("scheduler");
    $handler->detach();

    return 1;
}

######################################################################
# Main: Loop forever checking the DB every N
# minutes for reserversations
######################################################################
sub scheduler {

    print STDERR "Scheduler running\n";
    my ($dbHandle, $result);

    $dbHandle = BSS::Frontend::Database->new('configs' => $global_config);

    while (1) {

        # find reservations that need to be actived
        #print STDERR "find new\n";
        $result = find_new_reservations($dbHandle);
        if ($result == 0) {
            print STDERR "Error with find_new_res\n";
        }


        # find reservations that need to be deactivated 
        #print STDERR "find_exp\n";
        $result = find_expired_reservations($dbHandle);
        if ($result == 0) {
            print STDERR "Error with find_new_res\n";
        }

        # check every do_poll_time seconds
        sleep($global_config->{'db_poll_time'});
    }
}

######################################################################
# Find reservations to run
# need to find all the reservatations in db
# that need to be setup and run in the next N
# minutes
######################################################################
sub find_new_reservations {


    my ($dbHandle) = @_;

    #print STDERR "in find_new_res\n";
    my ($timeslot, $resv, $result);
    my $cur_time = localtime();

    #print STDERR "declared vars...\n";
    # configurable
    $timeslot = time() + $global_config->{'reservation_time_interval'};
    #print STDERR "pending: $cur_time \n";

    # find reservations that need to be scheduled
    $resv = $dbHandle->find_pending_reservations($timeslot, PENDING);

    foreach my $r (@$resv) {
        ## calls to pss to setup reservations
        $result = setup_pss($r, $dbHandle);

        #print STDERR "update reservation to active\n";
        update_reservation( $r, $result, ACTIVE, $dbHandle);
    }
    return 1;
}

######################################################################
#
# Find reservations that have expired, and tear them down
#
######################################################################
sub find_expired_reservations {

    my ($dbHandle) = @_;

    my $cur_time = localtime();
    my ($timeslot, $resv, $result);

    # configurable
    $timeslot = time() + $global_config->{reservation_time_interval};
    #print STDERR "expired: $cur_time \n";

    # find active reservation past the timeslot
    $resv = $dbHandle->find_expired_reservations($timeslot, ACTIVE);

    foreach my $r (@$resv) {
        $result = teardown_pss($r, $dbHandle);

        #print STDERR "update reservation to active\n";
        update_reservation( $r, $result, FINISHED, $dbHandle);
    }
    return 1;
}
######################################################################
#
# Format the args and call pss to do the configuraion change
#
######################################################################
sub setup_pss {

    my ($res, $dbHandle) = @_;

    # make those router idx's into ips
    my $srchost = $dbHandle->hostidx2ip( $res->{'src_hostaddrs_id'} );
    my $dsthost = $dbHandle->hostidx2ip( $res->{'dst_hostaddrs_id'} );
    my $srcrouter = $dbHandle->ipidx2ip( $res->{'ingress_interface_id'} );
    my $dstrouter = $dbHandle->ipidx2ip( $res->{'egress_interface_id'} );

    # probably a slick way with map to do this ...
    my (%_lspInfo) = (
      'name' => "oscars_$res->{'reservation_id'}",
      #'lsp_from' => $srcrouter,
      'lsp_from' => '198.128.1.138',
      #'lsp_to' => $dstrouter,
      'lsp_to' => '10.0.0.1',
      'bandwidth' => $res->{'reservation_bandwidth'},
      'lsp_class-of-service' => '4',
      'policer_burst-size-limit' =>  $res->{'reservation_burst_limit'},
      'source-address' => $srchost,
      'destination-address' => $dsthost,
#      'dscp' => 'ef',
#      'protocol' => 'udp',
#      'source-port' => '5000',
    );

    print STDERR "execing pss to schedule reservations\n";
    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($_jnxLsp) = new JnxLSP(%_lspInfo);

        print STDERR Dumper($_jnxLsp);
        print STDERR("Setting up LSP...\n");
        $_jnxLsp->configure_lsp(_LSP_SETUP);
        if ($_error = $_jnxLsp->get_error())  {
            print STDERR Dumper($_error);
            return 0;
            #die($_error);
        }
    }
    print STDERR("LSP setup complete\n");
    return 1;
}

######################################################################
#
# Format the args and call pss to teardown the configuraion 
#
######################################################################
sub teardown_pss {

    my ($res, $dbHandle) = @_;

    # make those router idx's into ips
    my $srchost = $dbHandle->hostidx2ip( $res->{'src_hostaddrs_id'});
    my $dsthost = $dbHandle->hostidx2ip( $res->{'dst_hostaddrs_id'});

    my $srcrouter = $dbHandle->ipidx2ip( $res->{'ingress_interface_id'});
    my $dstrouter = $dbHandle->ipidx2ip( $res->{'egress_interface_id'});

    #print STDERR "srchost $srchost, dsthost $dsthost, srcrout $srcrouter, dstr $dstrouter\n";

    # probably a slick way with map to do this ...
    my (%_lspInfo) = (
      'name' => "oscars_$res->{'reservation_id'}",
      'lsp_from' => $srcrouter,
      'lsp_to' => $dstrouter,
      'bandwidth' => $res->{'reservation_bandwidth'},
      'lsp_class-of-service' => '4',
      'policer_burst-size-limit' =>  $res->{'reservation_burst_limit'},
      'source-address' => $srchost,
      'destination-address' => $dsthost,
#      'dscp' => 'ef',
#      'protocol' => 'udp',
#      'source-port' => '5000',
    );

    #my $d = join ":", (values %_lspInfo) ;
    #print STDERR "$d \n";

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($_jnxLsp) = new JnxLSP(%_lspInfo);

        print STDERR("Tearing down LSP...\n");
        $_jnxLsp->configure_lsp(_LSP_TEARDOWN); 
        if ($_error = $_jnxLsp->get_error())  {
            return 0;
        }
    }
    print STDERR("LSP teardown complete\n");
    return 1;
}


######################################################################
#
# Change the status of the reservervation from pending to active
#
######################################################################

sub update_reservation {

    my ($resv, $result, $status, $dbHandle) = @_;

    if ( $result == 1 ) {
        print STDERR "Changing status to $status\n";
        $dbHandle->db_update_reservation($resv, $status)
    } else {
        print STDERR "Changing status to failed\n";
        $dbHandle->db_update_reservation($resv, FAILED)
    }
}

#########################
##  End of package
#########################
1;

# vim: et ts=4 sw=4
