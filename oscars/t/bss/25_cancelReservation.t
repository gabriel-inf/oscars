#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my( $status, $msg, $reservation_id );
my $db_name = 'AAAS';
my $aaa_component_name = 'AAAS';
my $bss_component_name = 'BSS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);

my( $login, $password ) = $rm->get_test_account('user');

# TODO:  get reservation id of reservation scheduled by $login
( $status, $msg ) = CancelReservation($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub CancelReservation {
    my( $user_dn, $password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    # Delete the reservation with the given id (set its status
    # to cancelled).
    $params{server} = $bss_component_name;
    $params{user_dn} = $user_dn;
    $params{method} = 'ManageReservations';
    $params{op} = 'cancelReservation';
    $params{reservation_id} = $reservation_id;

    my $som = $rm->add_client($aaa_component_name)->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________
