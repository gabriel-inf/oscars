######################################################################
# Scheduler thread that polls the db looking for 
# reservataions that need to be scheduled
#
# XXX: Poll time needs to come from a config file
#
# JRLee
######################################################################
package BSSScheduler;

use threads;
use threads::shared;

use strict;

# global config file 
my ($global_config);

###################################
# This should probably be just new or 
# some such OO thing. Just startup and deatch
# a thread to do scheduling, return control
# to the main prog
###################################
sub start_scheduler {

    $global_config = shift;

    print "Starting Scheduler\n";
    my $handler = threads->create("scheduler");
    $handler->detach();

    return 1;
}

###################################
# Loop forever checking the DB every N
# minutes for reserversations
###################################
sub scheduler {
    print "Scheduler running\n";
    while (1) {
        print "Scheduler looping\n";
        find_reservations();
        # check every 5 minutes or so
        sleep($global_config->{'db_poll_time'});
    }
}

###################################
# Find reservations to run
# need to find all the reservatations in db
# that need to be setup and run in the next N
# minutes
###################################
sub find_reservations {
    my $cur_time = localtime();

    # XXX: configurable: now 10 mins in future
    my $timeslot = time() + $global_config->{'reservation_time_interval'};
    
    print "searching db for reservations $cur_time \n";
    # dbcall to find reservations();
    #@reservaions = search_db_reservations($timeslot);

    #for $res (@reservations) {
        #print "res ==> $res \n";
        #print "execing pss to schedule reservations\n";
        ## calls to pss to setup reservations
        #print "update reservation to active\n";
        ## db call to update res
    #}

    return 1;
}

#########################
##  End of package
#########################
1;

# vim: et ts=4 sw=4
