#!/usr/bin/perl -w

# login.pl:  Main Service Login script
# Last modified: June 22, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

require 'general.pl';


my ($error_status, $results);
my ($user_dn, $user_level);
my $cgi = CGI->new();

# Check that the user exists, the correct password has been given, the user
# account has been activated, and the user has the proper privilege level
# to perform database operations.
($error_status, $results) = verify_user($cgi);

if (!$results->{error_msg}) {
    ($user_dn, $user_level) = check_session_status($results, $cgi);
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

    my( %soap_params, %results );

    # validate user input (just check for empty fields)
    if ( !$cgi->param('user_dn') )
    {
        $results{error_msg} = 'Please enter your login name.';
        return( 1, \%results );
    }

    if ( !$cgi->param('user_password') )
    {
        $results{error_msg} = 'Please enter your password.';
        return( 1, \%results );
    }
    $soap_params{user_dn} = $cgi->param('user_dn');
    $soap_params{user_password} = $cgi->param('user_password');
    return(soap_verify_login(\%soap_params));
}
######
