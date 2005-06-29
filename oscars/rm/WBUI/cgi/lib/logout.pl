#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: June 29, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;
use BSS::Client::SOAPClient;

require 'general.pl';


my $cgi = CGI->new();
my ($user_dn, $user_level) = check_session_status(undef, $cgi);

my ($error_status, $results);
# logout user from AAAS and BSS databases
($error_status, $results) = logout_user($user_dn);

# nuke session and put the user back at the login screen

if ($user_dn) { end_session($cgi); }

print '<script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>', "\n";
print '<script language="javascript" type="text/javascript" src="https://oscars.es.net/timeprint.js"></script>', "\n";
print '<script language="javascript">update_status_frame(1, "Please log in");</script>', "\n\n";
print '<script language="javascript">update_main_frame("https://oscars.es.net/login_frame.html");</script>', "\n\n";

exit;

##############################################################################
# logout_user:  Closes db connections to AAAS, BSS
# In:  user email address
# Out: error status, SOAP results
#
sub logout_user {
    my( $user_dn ) = @_;

    my( %soap_params, %error_only, $error_status, $results );
    my( $BSS_results );

    $soap_params{user_dn} = $user_dn;
    ($error_status, $results) = AAAS::SOAPClient::soap_logout(\%soap_params);
    if (!$results->{error_msg}) {
        ($error_status, $BSS_results) = BSS::SOAPClient::soap_logout_user(\%soap_params);
    }
    return( $error_status, $results );
}
######
