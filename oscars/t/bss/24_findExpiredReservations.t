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

my( $login, $password ) = $rm->get_test_account('engr');

( $status, $msg ) = FindExpiredReservations( $login, $password );
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub FindExpiredReservations {
    my ( $user_dn, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $bss_component_name;
    $params{method} = 'FindExpiredReservations';
    $params{user_dn} = $user_dn;
    $params{time_interval} = 20;

    my $som = $rm->add_client($aaa_component_name)->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
