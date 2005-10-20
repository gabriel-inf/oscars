#!/usr/bin/perl

####
# bss.pl:  Soap lite server for BSS
# Last modified:  October 19, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee (jrlee@lbl.gov)
###

#use SOAP::Lite +trace;
use SOAP::Lite;
use SOAP::Transport::HTTP;
use Data::Dumper;

## we want to thread on each accept, as some requests can take a 
## while (i.e. running traceroute)
#use SOAP::Transport::HTTP::Daemon::ThreadOnAccept;

# enable this in the 'production' version
# don't want to die on 'Broken pipe' or Ctrl-C
#$SIG{PIPE} = $SIG{INT} = 'IGNORE';

use BSS::Scheduler::SchedulerThread;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = BSS::Frontend::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

my $request_handler = BSS::Scheduler::ReservationHandler->new('dbconn' => $dbconn);

# start up a thread to monitor the DB
start_scheduler();

# Create a SOAP server
#my $daemon = SOAP::Transport::HTTP::Daemon::ThreadOnAccept
my $daemon = SOAP::Transport::HTTP::Daemon
    -> new (LocalPort => $dbconn->get_port(''), Listen => 5, Reuse => 1)
    -> dispatch_to('Dispatcher')
    -> handle;

######

package Dispatcher;

use Error qw(:try);
use Common::Exception;
use BSS::Frontend::Validator;
use BSS::Scheduler::ReservationHandler;

sub dispatch {
    my ( $class_name, $inref ) = @_;

    my ( $logging_buf, $ex );

    my $results = {};
    try {
        $v = BSS::Frontend::Validator->new();
        $v->validate($inref);
        my $m = $inref->{method};
        ($results, $logging_buf) = $request_handler->$m($inref) ;
    }
    catch Common::Exception with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
    }
    finally {
        my $logfile_name = "$ENV{HOME}/logs/";

        if ($results->{reservation_tag}) {
            $logfile_name .= $results->{reservation_tag};
        }
        else {
            $logfile_name .= "fatal_reservation_errors";
        }
        open (LOGFILE, ">$logfile_name") ||
                die "Can't open log file $logfile_name.\n";
        print LOGFILE "********************\n";
        if ($logging_buf) {
            print LOGFILE $logging_buf;
        }
        if ($ex) {
            print LOGFILE "EXCEPTION:\n";
            print LOGFILE $ex->{-text};
            print LOGFILE "\n";
        }
        close(LOGFILE);
    };
    # caught by SOAP to indicate fault
    if ($ex) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
}
######

######
1;
