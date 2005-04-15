#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 3000)
  -> dispatch_to('BSSServer')
;

$daemon->handle;

package BSSServer;

use lib '../..';

our @ISA = qw(Exporter);
our @EXPORT = qw(Create_reservation Remove_reservation Get_reservations);

use BSS::Frontend::Reservation;

sub Create_reservation {
  my ($class, %params) = @_;
  return (create_reservation(\%params));
}

sub Remove_reservation {
  my ($class, %params) = @_;
  return (remove_reservation(\%params));
}

sub Get_reservations {
  my ($class, %params) = @_;
  return (get_reservations(\%params));
}


