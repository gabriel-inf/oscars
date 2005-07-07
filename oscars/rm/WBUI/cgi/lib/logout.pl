#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: July 6, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;
use BSS::Client::SOAPClient;

require 'general.pl';


my $cgi = CGI->new();
my ($user_dn, $user_level, $oscars_home) = check_session_status(undef, $cgi);

# logout user from AAAS and BSS databases
my ($error_msg, $results) = logout_user($user_dn);

# nuke session and put the user back at the login screen

if ($user_dn) { end_session($cgi); }

print '<script language="javascript" type="text/javascript" src="' .
      $oscars_home . 'main_common.js"></script>' . "\n";
print '<script language="javascript" type="text/javascript" src="' .
      $oscars_home . 'timeprint.js"></script>', "\n";
print '<script language="javascript">update_status_frame(1, ' .
      '"Please sign in");</script>', "\n\n";
print '<script language="javascript">update_main_frame("' .
      $oscars_home . 'login_frame.html");</script>', "\n\n";

exit;

##############################################################################
# logout_user:  Closes db connections to AAAS, BSS
# In:  user email address
# Out: error status, SOAP results
#
sub logout_user {
    my( $user_dn ) = @_;

    my( %soap_params, $BSS_som );

    $soap_params{user_dn} = $user_dn;
    $soap_params{method} = 'soap_logout';
    my $som = AAAS::Client::SOAPClient::aaas_dispatcher(\%soap_params);
    if ($som->faultstring) {
        return( $som->faultstring, undef );
    }
    my $aaa_results = $som->result;
    if (!$aaa_results->{error_msg}) {
        $soap_params{method} = 'soap_logout_user';
        $BSS_som = BSS::Client::SOAPClient::bss_dispatcher(\%soap_params);
        return( "", $BSS_som->result );
    }
    else {
        return( $aaa_results->{error_msg}, undef );
    }
}
######
