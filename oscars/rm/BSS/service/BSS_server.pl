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

my $dbHandler;

sub dispatch {
    my ( $class_name, $inref ) = @_;

    my ( $logging_buf, $ex );

    my $results = {};
    if(!$dbHandler) {
        $dbHandler = BSS::Scheduler::ReservationHandler->new('configs' => $configs);
    }
    try {
        validate($inref);
        my $m = $inref->{method};
        ($results, $logging_buf) = $dbHandler->$m($inref) ;
    }
    catch Common::Exception with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
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

################################################################################# validate:  server-side validation
#
sub validate {
    my ( $inref ) = @_;

    if ($inref->{method} eq 'create_reservation') {
        if (!$inref->{reservation_start_time}) {
            throw Common::Exception("Please enter the reservation starting time");
        }
        if (!$inref->{reservation_end_time}) {
            throw Common::Exception("Please enter the reservation end time");
        }
        if (!$inref->{src_address}) {
            throw Common::Exception("Please enter starting host name or IP address");
        }
        if (!$inref->{dst_address}) {
            throw Common::Exception("Please enter destination host name or IP address");
        }
        if (!$inref->{reservation_bandwidth}) {
            throw Common::Exception("Please enter the bandwidth you wish to reserve");
        }
        if (!$inref->{reservation_description}) {
            throw Common::Exception("Please enter a description of the purpose for this reservation");
        }
        if ($inref->{src_address} eq $inref->{dst_address}) {
            throw Common::Exception("Please provide different addresses for the source and destination");
        }
        my @addr_sections = split('/', $inref->{src_address});
        if ($#addr_sections && ($addr_sections[1] < 24)) {
            throw Common::Exception("Only CIDR blocks >= 24 (class C) are accepted (source)");
        }
        @addr_sections = split('/', $inref->{dst_address});
        if ($#addr_sections && ($addr_sections[1] < 24)) {
            throw Common::Exception("Only CIDR blocks >= 24 (class C) are accepted (destination)");
        }
        if ($inref->{reservation_src_port}) {
            if (($inref->{reservation_src_port} < 1024) ||
                ($inref->{reservation_src_port} > 65535)) {
                throw Common::Exception("The source port, if given, must be in the range 1024-65535");
            }
        }
        if ($inref->{reservation_dst_port}) {
            if (($inref->{reservation_dst_port} < 1024) ||
                ($inref->{reservation_dst_port} > 65535)) {
                throw Common::Exception("The destination port, if given, must be in the range 1024-65535");
            }
        }
        if ($inref->{reservation_dscp}) {
            if (($inref->{reservation_dscp} < 0) ||
                 ($inref->{reservation_dscp} > 63)) {
                throw Common::Exception("The DSCP, if given, must be in the range 0-63");
            }
        }
    }
}
######

######
1;
