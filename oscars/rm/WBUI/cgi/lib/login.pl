#!/usr/bin/perl -w

# login.pl:  Main Service Login script
# Last modified: June 29, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

use AAAS::Client::SOAPClient;

require 'general.pl';

my $cgi = CGI->new();

# Check that the user exists, the correct password has been given, the user
# account has been activated, and the user has the proper privilege level
# to perform database operations.
my ($error_status, $results) = verify_user($cgi);

if (!$results->{error_msg}) {
    my ($user_dn, $user_level, $oscars_home) = check_session_status($results, $cgi);
    print_info($user_dn, $user_level);
    update_status_frame(0, $results->{status_msg});
}
else {
    update_status_frame(1, $results->{error_msg}); 
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

    my( %soap_params, %error_only, $error_status, $results );

    # validate user input (just check for empty fields)
    if ( !$cgi->param('user_dn') )
    {
        $error_only{error_msg} = 'Please enter your login name.';
        return( 1, \%error_only );
    }

    if ( !$cgi->param('user_password') )
    {
        $error_only{error_msg} = 'Please enter your password.';
        return( 1, \%error_only );
    }
    $soap_params{user_dn} = $cgi->param('user_dn');
    $soap_params{user_password} = $cgi->param('user_password');
    ($error_status, $results) = soap_verify_login(\%soap_params);
    return( $error_status, $results );
}
######
