#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;
use Data::Dumper;

use TestManager;

my $test_mgr = TestManager->new();
my $params = $test_mgr->get_params('aaa/29_managePermissions.xml');
my ($status, $msg) = ViewPermissions($test_mgr, $params);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub ViewPermissions {
    my( $test_mgr, $params ) = @_;

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved list of permissions\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
