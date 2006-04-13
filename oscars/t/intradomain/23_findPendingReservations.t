#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAA';
my $aaa_component_name = 'AAA';
my $bss_component_name = 'Intradomain';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');

my( $login, $password ) = $rm->get_test_account('testaccount');

my( $status, $msg ) = FindPendingReservations( $login, $password );
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub FindPendingReservations {
    my ( $user_login, $user_password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'FindPendingReservations';
    $params{time_interval} = 20;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
