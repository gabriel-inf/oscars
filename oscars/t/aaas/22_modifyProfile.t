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

my ($status, $msg) = ModifyProfile($login, $password);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub ModifyProfile {
    my( $user_login, $user_password ) = @_;

    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $component_name;
    $params{method} = 'UserProfile';
    $params{op} = 'modifyProfile';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Modify user profile\n";
    $msg .= "Profile now:\n\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________
