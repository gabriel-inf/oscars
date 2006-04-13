#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;
use OSCARS::Database;
use OSCARS::Intradomain::RouteHandler;

my $db_name = 'AAA';
my $aaa_component_name = 'AAA';
my $bss_component_name = 'Intradomain';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');

my( $login, $password ) = $rm->get_test_account('testaccount');

my $testdb = OSCARS::Database->new();
$testdb->connect('Intradomain');
my $rh = OSCARS::Intradomain::RouteHandler->new('user' => $testdb);
my $test_configs = $rh->get_test_configs('manageReservations');

my( $status, $msg ) = ViewReservations($login, $password);
ok( $status, $msg );
print STDERR $msg;


###############################################################################
#
sub ViewReservations {
    my ( $user_login, $user_password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'ViewReservations';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservations:\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
