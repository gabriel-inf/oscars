#!/usr/bin/perl

####
# Soap lite server for BSS
###
#use SOAP::Lite +trace;
use SOAP::Lite;
use SOAP::Transport::HTTP;

use BSSScheduler;
use BSS;

# point to my version of ThreadOnAccept
use lib "lib/perl5";

## we want to thread on each accept, as some requests can take a 
## while (i.e. running traceroute)
use SOAP::Transport::HTTP::Daemon::ThreadOnAccept;

use Config::Auto;

# enable this in the 'production' version
# don't want to die on 'Broken pipe' or Ctrl-C
#$SIG{PIPE} = $SIG{INT} = 'IGNORE';

# slurp up the config file
my $config = Config::Auto::parse('BSS.config');

# start up a thread to monitor the DB
BSSScheduler::start_scheduler($config);

# Let create a SOAP server
my $daemon = SOAP::Transport::HTTP::Daemon::ThreadOnAccept
	-> new (LocalPort => 8001, Listen => 5, Reuse => 1)
	-> dispatch_to('.', 'BSS')
	;
print "Contact to SOAP server at ", $daemon->url, "\n";
# and away we go
$daemon->handle;

# vim: et ts=4 sw=4

