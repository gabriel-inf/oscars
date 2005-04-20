#!/usr/bin/perl -w

####
# Soap lite server for BSS
###

#use SOAP::Lite +trace;
use SOAP::Lite;
use SOAP::Transport::HTTP;

use lib '../..';
use lib '/home/jason/oscars/trunk/oscars/rm/BSS/Scheduler/lib/perl5';

use BSS::Scheduler::SchedulerThread;

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
start_scheduler($config);

# Create a SOAP server
my $daemon = SOAP::Transport::HTTP::Daemon::ThreadOnAccept
	-> new (LocalPort => 3000, Listen => 5, Reuse => 1)
	-> dispatch_to('.', 'BSS_Server')
	;

# and away we go
$daemon->handle;


##########################

package BSS_Server;

use lib '../..';

our @ISA = qw(Exporter);
our @EXPORT = qw(Create_reservation Remove_reservation Get_reservations);

use BSS::Scheduler::ReservationHandler;
use BSS::Frontend::Reservation;

sub Create_reservation {
  my ($class, $inref ) = @_;
  return (create_reservation($inref));
}

sub Remove_reservation {
  my ($class, $inref) = @_;
  return (remove_reservation($inref));
}

sub Get_reservations {
  my ($class, $inref) = @_;
  return (get_reservations($inref));
}


# vim: et ts=4 sw=4

