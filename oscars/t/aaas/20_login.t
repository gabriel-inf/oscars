#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'AAAS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);

my( $login, $password ) = $rm->get_test_account('user');
my ($status, $msg) = Login($login, $password);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub Login {
    my( $user_dn, $user_password ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_password' => $user_password);
    $params{server} = $component_name;
    $params{method} = 'Login';

    my $som = $rm->add_client($component_name)->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    return( 1, "\nUser $params{user_dn} successfully logged in.\n" );
} #___________________________________________________________________________
