#!/usr/bin/perl

####
# Soap lite server for BSS
###

#use SOAP::Lite +trace;
use SOAP::Lite;
use SOAP::Transport::HTTP;

use Config::Auto;

## we want to thread on each accept, as some requests can take a 
## while (i.e. running traceroute)
#use SOAP::Transport::HTTP::Daemon::ThreadOnAccept;

# enable this in the 'production' version
# don't want to die on 'Broken pipe' or Ctrl-C
#$SIG{PIPE} = $SIG{INT} = 'IGNORE';

use BSS::Scheduler::SchedulerThread;
use BSS::Scheduler::ReservationHandler;

# slurp up the config file
our ($configs);

$configs = Config::Auto::parse($ENV{'OSCARS_HOME'} . '/oscars.cfg');

$db_handler = BSS::Scheduler::ReservationHandler->new('configs' => $configs);

# start up a thread to monitor the DB
start_scheduler($configs);

# Create a SOAP server
#my $daemon = SOAP::Transport::HTTP::Daemon::ThreadOnAccept
my $daemon = SOAP::Transport::HTTP::Daemon
	-> new (LocalPort => $configs->{'BSS_server_port'}, Listen => 5, Reuse => 1)
	-> dispatch_to('.', $db_handler)
	;

# and away we go
$daemon->handle;


# vim: et ts=4 sw=4

