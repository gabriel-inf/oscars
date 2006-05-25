#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 2;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::Database;
use Error qw{:try};

my $msg = "\n";
my $ex;
 
my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();
my $database = $configuration->{database}->{topology}->{location};
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
