#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 7;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $server_name = 'AAAS';
my $resource_manager = OSCARS::ResourceManager->new(
                                                'server_name' => $server_name);
ok($resource_manager);

my( $uri, $proxy ) = $resource_manager->get_proxy_info($server_name);
my $soap_proxy = $resource_manager->set_proxy( $uri, $proxy );

ok($soap_proxy);

    # TODO:  have completely unprivileged user, except for logging in
    #        so password here doesn't matter
my ($status, $msg) = Login('dwrobertson@lbl.gov', 'ritazza6');
ok($status, $msg);
print STDERR $msg;

($status, $msg) = GetProfile('dwrobertson@lbl.gov', 2);
ok($status, $msg);
print STDERR $msg;

($status, $msg) = ViewInstitutions('dwrobertson@lbl.gov', 2);
ok($status, $msg);
print STDERR $msg;

($status, $msg) = ViewPermissions('dwrobertson@lbl.gov', 15);
ok($status, $msg);
print STDERR $msg;

($status, $msg) = ViewUsers('dwrobertson@lbl.gov', 15);
ok($status, $msg);
print STDERR $msg;

##############################################################################
#
sub Login {
    my( $user_dn, $user_password ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_password' => $user_password);
    $params{server_name} = $server_name;
    $params{method} = 'Login';
    $params{user_level} = 2;

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    return( 1, "\nUser $params{user_dn} successfully logged in.\n" );
} #___________________________________________________________________________


##############################################################################
#
sub GetProfile {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'GetProfile';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved user profile\n";
    $msg .= to_string($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub SetProfile {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'SetProfile';
    $params{user_level} = 2;

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Set user profile\n";
    $msg .= "Profile now:\n\n" . to_string($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub ViewInstitutions {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'ViewInstitutions';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved list of institutions\n";
    for my $row (@$results) {
        $msg .= to_string($row);
    }
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub ViewPermissions {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'ViewPermissions';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved list of permissions\n";
    for my $row (@$results) {
        $msg .= to_string($row);
    }
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub AddUser {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'AddUser';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved user profile\n";
    $msg .= to_string($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub DeleteUser {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'DeleteUser';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Deleted user $user_dn\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub ViewUsers {
    my( $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = $server_name;
    $params{method} = 'ViewUsers';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Successfully read user list.\n";
    for my $row (@$results) {
        $msg .= to_string($row);
    }
    $msg .= "\n";
    return( 1, $msg );
}

##############################################################################
#
sub to_string {
    my( $results ) = @_;

    my( $key, $value );

    my $msg = '';
    foreach $key(sort keys %{$results} ) {
        if (($key ne 'status_msg') &&
            defined($results->{$key})) {
            $value = $results->{$key};
            if ($value) { $msg .= "$key -> $value\n"; }
            else { $msg .= "$key -> \n"; }
        }
    }
    return $msg;
} #___________________________________________________________________________
