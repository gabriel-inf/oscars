#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 2;
use Error qw{:try};

use OSCARS::PluginManager;
use OSCARS::Database;

my( $ex, $msg );
 
my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();
my $database = $configuration->{database}->{'system'}->{location};
my $dbconn = OSCARS::Database->new();
ok( $dbconn );

try {
    $dbconn->connect($database);
}
catch Error::Simple with { $ex = shift; }
otherwise { $ex = shift; }
finally {
    if ($ex) { print STDERR "\n" . $ex->{-text} . "\n"; }
    ok( !$ex, $msg );
};
