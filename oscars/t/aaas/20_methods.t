#!/usr/bin/perl -w

use strict;
use Test qw(plan ok skip);

plan tests => 8;

use SOAP::Lite;
use Term::ReadKey;
use Data::Dumper;

ReadMode('noecho');
print STDERR "Please enter your OSCARS password: ";
my $password = ReadLine(0);
ReadMode('restore');
print STDERR "\n";
chomp($password);

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => $password);
$params{server_name} = 'AAAS';
$params{method} = 'login';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n\n";
    exit;
}
my $results = $som->result;
print "\nUser $params{user_dn} successfully logged in.\n\n";

##############################################################################

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{server_name} = 'AAAS';
$params{method} = 'get_profile';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results} )
{
    if (($key ne 'status_msg') &&
        defined($results->{$key}))
    {
        $value = $results->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}

##############################################################################

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{server_name} = 'AAAS';
$params{method} = 'set_profile';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results} )
{
    if (($key ne 'status_msg') &&
        defined($results->{$key}))
    {
        $value = $results->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}

##############################################################################

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{server_name} = 'AAAS';
$params{method} = 'view_institutions';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results} )
{
    if (($key ne 'status_msg') &&
        defined($results->{$key}))
    {
        $value = $results->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}

##############################################################################

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{server_name} = 'AAAS';
$params{method} = 'view_permissions';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results} )
{
    if (($key ne 'status_msg') &&
        defined($results->{$key}))
    {
        $value = $results->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}

##############################################################################

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{server_name} = 'AAAS';
$params{method} = 'add_user';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results} )
{
    if (($key ne 'status_msg') &&
        defined($results->{$key}))
    {
        $value = $results->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}

##############################################################################

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{server_name} = 'AAAS';
$params{method} = 'delete_user';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results} )
{
    if (($key ne 'status_msg') &&
        defined($results->{$key}))
    {
        $value = $results->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}

##############################################################################

my($data, $row, $key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'admin' );
$params{server_name} = 'AAAS';
$params{method} = 'view_users';
$params{user_level} = 8;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}
my $results = $som->result;
print "Status:  Successfully read user list.\n";
print "Returning:\n\n";
for $row (@$results) {
    for $key (sort keys %$row )
    {
        $value = $row->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
    print "\n";
}
