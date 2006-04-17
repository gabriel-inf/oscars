#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;
use Data::Dumper;

use TestManager;

my $test_mgr = TestManager->new();
my $params = $test_mgr->get_params('aaa/22_modifyProfile.xml');
my ($status, $msg) = ModifyProfile($test_mgr, $params);
ok($status, $msg);
print STDERR $msg;

##############################################################################
#
sub ModifyProfile {
    my( $test_mgr, $params ) = @_;

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Modify user profile\n";
    $msg .= "Profile now:\n\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________
