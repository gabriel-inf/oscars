#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 6;

use SOAP::Lite;
use Data::Dumper;

my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

ok($soap_server);

    # TODO:  have completely unprivileged user, except for logging in
    #        so password here doesn't matter
my ($status, $msg) = login($soap_server, 'dwrobertson@lbl.gov',
                                'ritazza6');
ok($status, $msg);
print STDERR $msg;

($status, $msg) = get_profile($soap_server, 'dwrobertson@lbl.gov', 2);
ok($status, $msg);
print STDERR $msg;

($status, $msg) = view_institutions($soap_server, 'dwrobertson@lbl.gov', 2);
ok($status, $msg);
print STDERR $msg;

($status, $msg) = view_permissions($soap_server, 'dwrobertson@lbl.gov', 15);
ok($status, $msg);
print STDERR $msg;

($status, $msg) = view_users($soap_server, 'dwrobertson@lbl.gov', 15);
ok($status, $msg);
print STDERR $msg;

##############################################################################
#
sub login {
    my( $soap_server, $user_dn, $user_password ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_password' => $user_password);
    $params{server_name} = 'AAAS';
    $params{method} = 'login';
    $params{user_level} = 2;

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    return( 1, "\nUser $params{user_dn} successfully logged in.\n" );
} #___________________________________________________________________________


##############################################################################
#
sub get_profile {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'get_profile';

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved user profile\n";
    $msg .= to_string($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub set_profile {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'set_profile';
    $params{user_level} = 2;

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;
    my $msg = "\nStatus:  Set user profile\n";
    $msg .= "Profile now:\n\n" . to_string($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub view_institutions {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'view_institutions';

    my $som = $soap_server->dispatch(\%params);
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
sub view_permissions {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'view_permissions';

    my $som = $soap_server->dispatch(\%params);
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
sub add_user {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'add_user';

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Retrieved user profile\n";
    $msg .= to_string($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub delete_user {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'delete_user';

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus:  Deleted user $user_dn\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub view_users {
    my( $soap_server, $user_dn, $user_level ) = @_;

    my %params = ('user_dn' => $user_dn, 'user_level' => $user_level );
    $params{server_name} = 'AAAS';
    $params{method} = 'view_users';

    my $som = $soap_server->dispatch(\%params);
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
