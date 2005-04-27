# general.pl
#
# library for general cgi script usage
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)


use CGI;

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;
use BSS::Client::SOAPClient;


# contact info
$webmaster = 'dwrobertson@lbl.gov';

$login_URI = 'https://oscars.es.net/';


sub check_login {
  my( $set_cookie, $cgi ) = @_;
  my( $login_URI, $auth );

  $auth = AAAS::Client::Auth->new();

  if ( $set_cookie )
  {
      $auth->set_login_status($cgi);
  }
  elsif (!($auth->verify_login_status($cgi))) 
  {
      print "Location: $login_URI\n\n";
      return ( "Please try a different login name." );
  }
  return ( '' );
}



##### sub update_status_frame
##    Prints to the status frame, and sets location of the main frame if
##    a URI is given.
#
##    In:  uri, error or status msg
#####
sub update_frames
{
  my ($target, $uri, $msg) = @_;
  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "<script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "</head>\n";
  print "<body>\n";
  print "<script language=\"javascript\">update_status_message(\"$target\", \"$msg\");</script>";
  if ($uri) {
    print "<script language=\"javascript\">update_main_frame(\"$uri\", \"$msg\");</script>";
  }
  print "</body>\n";
  print "</html>\n";
  print "\n\n";
}


sub end_session {
  my( $cgi ) = @_;
  my( $auth );

  $auth = AAAS::Client::Auth->new();
  $auth->logout($cgi);
}



# Don't touch the line below
1;
