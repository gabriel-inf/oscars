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

# TODO:  get reservation id of reservation scheduled by $login
( $status, $msg ) = GetEdges($login, $password);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub GetEdges {
    my( $user_dn, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $bss_component_name;
    $params{user_dn} = $user_dn;
    $params{method} = 'ManageEdges';
    $params{domain_str} = 'abilene';

    my $som = $rm->add_client($aaa_component_name)->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nSuccessfully retrieved edges\n";
    $msg .= Dumper($results);
    return( 1, $msg );
} #___________________________________________________________________________
