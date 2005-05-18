#!/usr/bin/perl -w

# login.pl:  Main Service Login script
# Last modified: May 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

require 'general.pl';


$startpoint = 'https://oscars.es.net/';

my ($error_status, %results);
my $cgi = CGI->new();

# Check that the user exists, the correct password has been given, the user
# account has been activated, and the user has the proper privilege level
# to perform database operations.
($error_status, %results) = verify_user($cgi);

if (!$results{'error_msg'}) {
    check_session_status(\%results, $cgi);
    if ($cgi->param('admin_required')) {
        update_frames(0, "status_frame", $startpoint . '/admin/gateway.html', $cgi->param('user_dn') . " logged in as administrator");
    }
    else {
        update_frames(0, "status_frame", $startpoint . '/user/', $cgi->param('user_dn'). " logged in");
    }
}
else {
    update_frames(1, "status_frame", "", $results{'error_msg'}); 
}

exit;


##### sub verify_user
# In:  CGI instance
# Out: error status, SOAP results
sub verify_user
{
    my( $cgi ) = @_;
    my( %soap_params, %results );

    # validate user input (just check for empty fields)
    if ( $cgi->param('user_dn') eq '' )
    {
        $results{'error_msg'} = 'Please enter your login name.';
        return( 1, %results );
    }

    if ( $cgi->param('user_password') eq '' )
    {
        $results{'error_msg'} = 'Please enter your password.';
        return( 1, %results );
    }
    $soap_params{'user_dn'} = $cgi->param('user_dn');
    $soap_params{'admin_required'} = $cgi->param('admin_required');
    $auth = AAAS::Client::Auth->new();
    #$soap_params{'user_password'} = $auth->encode_passwd($cgi->param('user_password'));
    $soap_params{'user_password'} = $cgi->param('user_password');

    return(soap_verify_login(\%soap_params));
}
