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

# TODO:  select reservation(s) with $login as owner to get reservation id
( $status, $msg ) = Reservation($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub Reservation {
    my ( $user_dn, $password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $bss_component_name;
    $params{method} = 'Reservation';
    $params{reservation_id} = $reservation_id;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservation details:\n";
    $msg .= Dumper($results);
    return( 1, $msg );
} #___________________________________________________________________________
