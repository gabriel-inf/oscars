#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 2;

use OSCARS::PluginManager;
use OSCARS::Database;
use Error qw{:try};

my $msg = "\n";
my $ex;
 
my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getDatabase('Intradomain');
my $dbconn = OSCARS::Database->new();
ok($dbconn);

try {
    $dbconn->connect($database);
}
catch Error::Simple with { $ex = shift; }
otherwise { $ex = shift; }
finally {
    if ($ex) {
        $msg .= $ex->{-text};
    }
    else { 
        $msg .= "Successful database connection";
    }
    $msg .= "\n";
    print STDERR $msg;
    ok( !$ex, $msg );
};
