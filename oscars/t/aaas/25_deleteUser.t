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

my ($status, $msg) = DeleteUser($login, $password, 'temptest');
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub DeleteUser {
    my( $user_dn, $password, $delete_user_dn ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $component_name;
    $params{method} = 'ManageUsers';
    $params{op} = 'deleteUser';

    # User with user_dn = id will be deleted
    $params{selected_user} = $delete_user_dn;

    my $som = $rm->add_client($component_name)->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Deleted user $delete_user_dn\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
