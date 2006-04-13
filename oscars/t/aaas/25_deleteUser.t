#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAA';
my $component_name = 'AAA';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');

my( $login, $password ) = $rm->get_test_account('testaccount');

my ($status, $msg) = DeleteUser($login, $password, 'temptest');
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub DeleteUser {
    my( $user_login, $user_password, $delete_user_login ) = @_;

    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $component_name;
    $params{method} = 'ManageUsers';
    $params{op} = 'deleteUser';

    # User with user_login = id will be deleted
    $params{selected_user} = $delete_user_login;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Deleted user $delete_user_login\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
