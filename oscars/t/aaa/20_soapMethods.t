#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 10;

use Data::Dumper;

use TestManager;

my $testMgr = TestManager->new();
my $params = $testMgr->getParams('aaa/params.xml');

my ($status, $msg) = login($testMgr, $params->{login});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userProfile($testMgr, $params->{queryProfile});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = modifyProfile($testMgr, $params->{modifyProfile});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = addUser($testMgr, $params->{addUser});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = deleteUser($testMgr, $params->{deleteUser});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = listUsers($testMgr, $params->{listUsers});
ok($status, $msg);
print STDERR $msg;

($status, $msg) =
    listAuthorizations($testMgr, $params->{listAuthorizations});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = listResources($testMgr, $params->{listResources});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = listPermissions($testMgr, $params->{listPermissions});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = logout($testMgr, $params->{logout});
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub login {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    return( 1, "\nUser $params->{login} successfully logged in.\n" );
} #___________________________________________________________________________


##############################################################################
#
sub userProfile {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    my $msg = "\nStatus:  Retrieved user profile\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub modifyProfile {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    my $msg = "\nStatus:  Modify user profile\n";
    $msg .= "Profile now:\n\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub addUser {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Successfully added user $params->{selectedUser}\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub deleteUser {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Deleted user $params->{selectedUser}\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub listUsers {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Successfully read user list.\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub listAuthorizations {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Retrieved user authorizations\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub listResources {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Retrieved resources successfully\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub listPermissions {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Retrieved list of permissions\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub logout {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    return( 1, "\nUser $params->{login} successfully logged out.\n" );
} #___________________________________________________________________________

