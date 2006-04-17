#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;
use Data::Dumper;

use TestManager;

my $test_mgr = TestManager->new();
my $params = $test_mgr->get_params('aaa/24_addUser.xml');
print STDERR Dumper($params);
my ($status, $msg) = AddUser($test_mgr, $params);
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub AddUser {
    my( $test_mgr, $params ) = @_;

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Successfully added user $params->{selected_user}\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________

