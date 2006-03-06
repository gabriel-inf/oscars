#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $aaa_component_name = 'AAAS';
my $bss_component_name = 'BSS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);

my( $login, $password ) = $rm->get_test_account('engr');

my( $status, $msg ) = UpdateRouterTables($login, $password);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub UpdateRouterTables {
    my( $user_dn, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $bss_component_name;
    $params{method} = 'UpdateRouterTables'; 
    $params{directory} = '/home/oscars/ifrefpoll';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus: Successfully updated router tables.\n";
    $msg .= Dumper($results);
    return( 1, $msg );
} #___________________________________________________________________________
