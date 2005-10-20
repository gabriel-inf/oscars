#!/usr/bin/perl -w

# login.pl:  Main Service Login script
# Last modified: August 12, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;
use Data::Dumper;

use Common::Auth;
use AAAS::Client::SOAPClient;

require 'general.pl';

my $cgi = CGI->new();

# Check that the user exists, the correct password has been given, the user
# account has been activated, and the user has the proper privilege level
# to perform database operations.
$som = verify_user($cgi);
if ($som->faultstring) {
    print $cgi->header( -type=>'text/xml' );
    update_page($som->faultstring); 
    exit;
}
$results = $som->result;
my( $user_dn, $user_level, $sid );

my $auth = Common::Auth->new();
($user_dn, $user_level, $sid) = $auth->start_session($cgi, $results);
print $cgi->header( -type=>'text/xml', -cookie=>$cgi->cookie(CGISESSID => $sid) );
update_page("User $user_dn signed in.", \&output_info, $user_dn, $user_level);
exit;

######


##############################################################################
# verify_user:  Checks if user has an account
# In:  CGI instance
# Out: error status, SOAP results
#
sub verify_user {
    my( $cgi ) = @_;

    my( %soap_params );

    $soap_params{user_dn} = $cgi->param('user_dn');
    $soap_params{user_password} = $cgi->param('user_password');
    $soap_params{method} = 'verify_login'; 
    return ( aaas_dispatcher(\%soap_params) );
}
######

######
1;
