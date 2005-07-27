#!/usr/bin/perl -w

# login.pl:  Main Service Login script
# Last modified: July 13, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

use AAAS::Client::SOAPClient;

require 'general.pl';

my $cgi = CGI->new();

# Check that the user exists, the correct password has been given, the user
# account has been activated, and the user has the proper privilege level
# to perform database operations.
$som = verify_user($cgi);
$results = $som->result;

my ($user_dn, $user_level, $oscars_home) = check_session_status($results, $cgi);
print_info($user_dn, $user_level);
update_status_frame(0, "User $user_dn successfully logged in.");
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

    # validate user input (just check for empty fields)
    if ( !$cgi->param('user_dn') ) {
        update_status_frame(1, 'Please enter your login name'); 
        exit;
    }
    if ( !$cgi->param('user_password') ) {
        update_status_frame(1, 'Please enter your password'); 
        exit;
    }
    $soap_params{user_dn} = $cgi->param('user_dn');
    $soap_params{user_password} = $cgi->param('user_password');
    $soap_params{method} = 'verify_login'; 
    my $som = aaas_dispatcher(\%soap_params);
    if ($som->faultstring) {
        update_status_frame(1, $som->faultstring); 
        exit;
    }
    return ( $som );
}
######
