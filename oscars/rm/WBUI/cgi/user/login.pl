#!/usr/bin/perl -w

# login.pl:  Main Service Login page
# Last modified: April 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

require '../lib/general.pl';


$service_startpoint_URI = 'https://oscars.es.net/user/';

my ($error_status, %results);
my $cgi = CGI->new();

($error_status, %results) = check_db_user($cgi);

if ($results{'error_msg'}) { $error_status = 1; }
else { $error_status = 0; }

if (!$error_status) {
    $error_status = check_login(\%results, $cgi);
    update_frames($error_status, "status_frame", $service_startpoint_URI, "Logged in as " . $cgi->param('dn') . ".");
}
else {
    update_frames($error_status, "status_frame", "", $results{'error_msg'});
}

exit;


##### sub check_db_user
# In:  CGI instance
# Out: error status, SOAP results
sub check_db_user
{
    my( $cgi ) = @_;
    my( %soap_params, %results );

    # validate user input (just check for empty fields)
    if ( $cgi->param('dn') eq '' )
    {
        $results{'error_msg'} = 'Please enter your login name.';
        return( 1, %results );
    }

    if ( $cgi->param('password') eq '' )
    {
        $results{'error_msg'} = 'Please enter your password.';
        return( 1, %results );
    }
    $soap_params{'dn'} = $cgi->param('dn');
    $auth = AAAS::Client::Auth->new();
    $soap_params{'password'} = $auth->encode_passwd($cgi->param('password'));

    return(soap_verify_login(\%soap_params));
}
