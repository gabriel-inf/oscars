#!/usr/bin/perl -w

# login.pl:  Main Service Login script
# Last modified: July 6, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

use AAAS::Client::SOAPClient;

require 'general.pl';

my $cgi = CGI->new();

# Check that the user exists, the correct password has been given, the user
# account has been activated, and the user has the proper privilege level
# to perform database operations.
my ($error_msg, $results) = verify_user($cgi);

if (!$error_msg) {
    my ($user_dn, $user_level, $oscars_home) = check_session_status($results, $cgi);
    print_info($user_dn, $user_level);
    update_status_frame(0, "User $user_dn successfully logged in.");
}
else {
    update_status_frame(1, $error_msg); 
}
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
        return( 'Please enter your login name', undef );
    }
    if ( !$cgi->param('user_password') ) {
        return( 'Please enter your password.', undef );
    }
    $soap_params{user_dn} = $cgi->param('user_dn');
    $soap_params{user_password} = $cgi->param('user_password');
    $soap_params{method} = 'soap_verify_login'; 
    my $som = aaas_dispatcher(\%soap_params);
    if ($som->faultstring) {
        return( $som->faultstring, undef );
    }
    return( "", $som->result );
}
######
