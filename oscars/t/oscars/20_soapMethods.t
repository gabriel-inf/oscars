#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 10;

use Data::Dumper;

use TestManager;
use OSCARS::Logger;

my $testMgr = TestManager->new();
my $params = $testMgr->getParams('oscars/params.xml');

ok( userLogin($testMgr, $params->{userLogin}) );
ok( authorizationList($testMgr, $params->{authorizationList}) );
ok( permissionList($testMgr, $params->{permissionList}) );
ok( resourceList($testMgr, $params->{resourceList}) );
ok( userQuery($testMgr, $params->{userQuery}) );
ok( userAdd($testMgr, $params->{userAdd}) );
ok( userModify($testMgr, $params->{userModify}) );
ok( userRemove($testMgr, $params->{userRemove}) );
ok( userList($testMgr, $params->{userList}) );
ok( userLogout($testMgr, $params->{userLogout}) );

##############################################################################
#
sub userLogin {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userLogin');
} #___________________________________________________________________________


##############################################################################
#
sub authorizationList {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'authorizationList');
} #___________________________________________________________________________


##############################################################################
#
sub userAdd {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userAdd');
} #___________________________________________________________________________


##############################################################################
#
sub userRemove {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userRemove');
} #___________________________________________________________________________


##############################################################################
#
sub userModify {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userModify');
} #___________________________________________________________________________


##############################################################################
#
sub userQuery {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userQuery');
} #___________________________________________________________________________


##############################################################################
#
sub userList {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userList');
} #___________________________________________________________________________


##############################################################################
#
sub resourceList {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'resourceList');
} #___________________________________________________________________________


##############################################################################
#
sub permissionList {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'permissionList');
} #___________________________________________________________________________


##############################################################################
#
sub userLogout {
    my( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'userLogout');
} #___________________________________________________________________________

