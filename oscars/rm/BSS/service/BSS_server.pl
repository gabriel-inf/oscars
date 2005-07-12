#!/usr/bin/perl

####
# BSS_server.pl:  Soap lite server for BSS
# Last modified:  July 8, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee (jrlee@lbl.gov)
###

#use SOAP::Lite +trace;
use SOAP::Lite;
use SOAP::Transport::HTTP;
use Data::Dumper;
use Config::Auto;

## we want to thread on each accept, as some requests can take a 
## while (i.e. running traceroute)
#use SOAP::Transport::HTTP::Daemon::ThreadOnAccept;

# enable this in the 'production' version
# don't want to die on 'Broken pipe' or Ctrl-C
#$SIG{PIPE} = $SIG{INT} = 'IGNORE';

use BSS::Scheduler::ReservationHandler;
use BSS::Scheduler::SchedulerThread;

# slurp up the config file
our ($configs);

$configs = Config::Auto::parse($ENV{'OSCARS_HOME'} . '/oscars.cfg');

my $dbHandler = BSS::Scheduler::ReservationHandler->new('configs' => $configs);

# start up a thread to monitor the DB
start_scheduler($configs);

# Create a SOAP server
#my $daemon = SOAP::Transport::HTTP::Daemon::ThreadOnAccept
my $daemon = SOAP::Transport::HTTP::Daemon
    -> new (LocalPort => $configs->{'BSS_server_port'}, Listen => 5, Reuse => 1)
    -> dispatch_to('Dispatcher')
    -> handle;

######

package Dispatcher;

use Error qw(:try);
use Common::Exception;

sub dispatch {
    my ( $self, $inref ) = @_;

    my ( $logging_buf );

    my $results = {};
    try {
        if ($inref->{method} eq 'soap_logout_user') {
            $results = $dbHandler->logout($inref) ;
        }
        elsif ($inref->{method} eq 'soap_get_reservations') {
            $results = $dbHandler->get_reservations($inref) ;
        }
        elsif ($inref->{method} eq 'soap_create_reservation') {
            ($results, $logging_buf) = $dbHandler->create_reservation($inref) ;
        }
        elsif ($inref->{method} eq 'soap_delete_reservation') {
            $results = $dbHandler->delete_reservation($inref) ;
        }
        else {
            if ($inref->{method}) {
                $results->{error_msg} = "No such SOAP method: $inref->{method}\n";
            }
            else {
                $results->{error_msg} = "SOAP method not provided\n";
            }
        }
    }
    catch Common::Exception with {
        my $E = shift;
        print STDERR $E->{-text}, "\n";
        $results->{error_msg} = $E->{-text};
    }
    finally {
        my $logfile_name = "$ENV{OSCARS_HOME}/logs/";

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
        if ($results->{error_msg}) {
            print LOGFILE "EXCEPTION:\n";
            print LOGFILE $results->{error_msg};
            print LOGFILE "\n";
        }
        close(LOGFILE);
    };
    return $results;
}
######
