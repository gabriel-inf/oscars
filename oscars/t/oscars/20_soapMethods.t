#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 10;

use Data::Dumper;

use TestManager;

my $testMgr = TestManager->new();
my $params = $testMgr->getParams('oscars/params.xml');

my ($status, $msg) = userLogin($testMgr, $params->{userLogin});
ok($status, $msg);
print STDERR $msg;

($status, $msg) =
    authorizationList($testMgr, $params->{authorizationList});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = permissionList($testMgr, $params->{permissionList});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = resourceList($testMgr, $params->{resourceList});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userQuery($testMgr, $params->{userQuery});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userAdd($testMgr, $params->{userAdd});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userModify($testMgr, $params->{userModify});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userRemove($testMgr, $params->{userRemove});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userList($testMgr, $params->{userList});
ok($status, $msg);
print STDERR $msg;

($status, $msg) = userLogout($testMgr, $params->{userLogout});
ok($status, $msg);
print STDERR $msg;


##############################################################################
#
sub userLogin {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userLogin');
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    return( 1, "\nUser $params->{login} successfully logged in.\n" );
} #___________________________________________________________________________


##############################################################################
#
sub authorizationList {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'authorizationList');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Retrieved user authorizations\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub userAdd {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userAdd');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Successfully added user $params->{selectedUser}\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub userRemove {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userRemove');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Removed user $params->{selectedUser}\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub userModify {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userModify');
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    my $msg = "\nStatus:  Modify user profile\n";
    $msg .= "Profile now:\n\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub userQuery {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userQuery');
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    my $msg = "\nStatus:  Retrieved user profile\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub userList {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userList');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Successfully read user list.\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub resourceList {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'resourceList');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Retrieved resources successfully\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub permissionList {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'permissionList');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus:  Retrieved list of permissions\n";
    $msg .= Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub userLogout {
    my( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'userLogout');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    return( 1, "\nUser $params->{login} successfully logged out.\n" );
} #___________________________________________________________________________

