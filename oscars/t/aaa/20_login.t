#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use Data::Dumper;

use TestManager;

my $test_mgr = TestManager->new();
my $params = $test_mgr->get_params('aaa/20_login.xml');
my ($status, $msg) = Login($test_mgr, $params);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub Login {
    my( $test_mgr, $params ) = @_;

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    return( 1, "\nUser $params->{user_login} successfully logged in.\n" );
} #___________________________________________________________________________
