#!/usr/bin/perl -w

####
# Soap lite server for BSS
###

#use SOAP::Lite +trace;
use SOAP::Lite;
use SOAP::Transport::HTTP;

use lib '../..';
use lib '../Scheduler/lib/perl5';

## we want to thread on each accept, as some requests can take a 
## while (i.e. running traceroute)
use SOAP::Transport::HTTP::Daemon::ThreadOnAccept;

# enable this in the 'production' version
# don't want to die on 'Broken pipe' or Ctrl-C
#$SIG{PIPE} = $SIG{INT} = 'IGNORE';

use BSS::Scheduler::SchedulerThread;
use BSS::Scheduler::ReservationHandler;

use Config::Auto;

# slurp up the config file
our ($config, $dbUser);

$config = Config::Auto::parse('BSS.config');

$db_handler = BSS::Scheduler::ReservationHandler->new('configs' => $config);

# start up a thread to monitor the DB
start_scheduler($config, $db_handler);

# Create a SOAP server
#my $daemon = SOAP::Transport::HTTP::Daemon::ThreadOnAccept
my $daemon = SOAP::Transport::HTTP::Daemon
	-> new (LocalPort => $config->{'server_port'}, Listen => 5, Reuse => 1)
	-> dispatch_to('.', $db_handler)
	;

# and away we go
$daemon->handle;


# vim: et ts=4 sw=4

