#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'AAAS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->set_authentication_style('OSCARS::AAAS::AuthN', 'AAAS');

my( $login, $password ) = $rm->get_test_account('testaccount');

my ($status, $msg) = ViewAuthorizations($login, $password);
ok($status, $msg);
print STDERR $msg;

##############################################################################
#
sub ViewAuthorizations {
    my( $user_login, $user_password ) = @_;

    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $component_name;
    $params{method} = 'ManageAuthorizations';
    $params{op} = 'viewAuthorizations';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved user authorizations\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
