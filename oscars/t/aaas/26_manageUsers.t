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

my ($status, $msg) = ManageUsers($login, $password);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub ManageUsers {
    my( $user_dn, $password ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $component_name;
    $params{method} = 'ManageUsers';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Successfully read user list.\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________

