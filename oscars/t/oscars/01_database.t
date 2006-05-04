#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 2;
use Error qw{:try};

use OSCARS::PluginManager;
use OSCARS::Database;

my( $ex, $msg );
 
my $pluginMgr = OSCARS::PluginManager->new();
my $dbconn = OSCARS::Database->new();
ok($dbconn);

try {
    my $dbName = $pluginMgr->getLocation('system');
    $dbconn->connect($dbName);
}
catch Error::Simple with { $ex = shift; }
otherwise { $ex = shift; }
finally {
    if ($ex) { print STDERR "\n" . $ex->{-text} . "\n"; }
    ok( !$ex, $msg );
};
