#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 2;

use OSCARS::PluginManager;
use OSCARS::Database;
use Error qw{:try};

my $msg = "\n";
my $ex;
 
my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getLocation('topology');
my $dbconn = OSCARS::Database->new();
ok($dbconn);

try {
    $dbconn->connect($database);
}
catch Error::Simple with { $ex = shift; }
otherwise { $ex = shift; }
finally {
    if ($ex) {
        print STDERR "\n" . $ex->{-text} . "\n";
    }
    ok( !$ex );
};
