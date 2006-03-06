#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'AAAS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my( $login, $password ) = $rm->get_test_account('admin');

my ($status, $msg) = AddUser($login, $password, 'temptest');
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub AddUser {
    my( $user_dn, $password, $new_user_dn ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $component_name;
    $params{method} = 'ManageUsers';
    $params{op} = 'addUser';

    $params{selected_user} = $new_user_dn;
    $params{password_new_once} = 'ac@demy';
    $params{user_last_name} = 'User';
    $params{user_first_name} = 'Temp';
    $params{institution_name} = 'NERSC';
    $params{user_email_primary} = 'dwrobertson@lbl.gov';
    $params{user_phone_primary} = '510-495-2399';
    $params{user_description} = 'test user';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Successfully add user $new_user_dn\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
