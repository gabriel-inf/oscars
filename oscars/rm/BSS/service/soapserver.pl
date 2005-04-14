#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 3000)
  -> dispatch_to('BSSServer')
;

$daemon->handle;

package BSServer;

use lib '../..';

our @ISA = qw(Exporter);
our @EXPORT = qw(get_user_reservations process_user_reservation);

use BSS::Frontend::Reservation;

sub get_user_reservations {
  my ($class, %params) = @_;
  return (get_reservations(\%params));
}


sub process_user_reservation {
  my ($class, %params) = @_;
  return (process_reservation(\%params));
}

