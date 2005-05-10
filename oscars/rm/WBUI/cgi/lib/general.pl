# general.pl
#
# library for general cgi script usage
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;
use DateTime;
use Socket;

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;
use BSS::Client::SOAPClient;


# contact info
$webmaster = 'dwrobertson@lbl.gov';

$login_URI = 'https://oscars.es.net/';


# checks session status only; database check done elsewhere

sub check_login {
    my( $login_results, $cgi ) = @_;
    my( $auth );

    $auth = AAAS::Client::Auth->new();
    if ( $login_results ) {
        $auth->set_login_status($cgi);
    }
    elsif (!($auth->verify_login_status($cgi))) {
        return ( 1 );
    }
    return ( 0 );
}


##### sub update_status_frame
##    Prints to the status frame, and sets location of the main frame if
##    a URI is given.
#
##    In:  uri, error or status msg
#####
sub update_frames
{
    my ($error_made, $target, $uri, $msg) = @_;
    if ($error_made) { print "Content-type: text/html\n\n"; }
    print "<html>\n";
    print "<head>\n";
    print "<link rel=\"stylesheet\" type=\"text/css\" ";
    print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
    print "<script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
    print "</head>\n";
    print "<body>\n";
    print "<script language=\"javascript\">update_status_message(\"$target\", \"$msg\");</script>";
    if ($uri) {
        print "<script language=\"javascript\">update_frame(\"main_frame\", \"$uri\");</script>";
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


sub get_time_str 
{
    my( $epoch_seconds ) = @_;

    my $dt = DateTime->from_epoch( epoch => $epoch_seconds );
    my $year = $dt->year();
    if ($year < 10) {
        $year = "0" . $year;
    }
    my $month = $dt->month();
    if ($month < 10) {
        $month = "0" . $month;
    }
    my $day = $dt->day();
    if ($day < 10) {
        $day = "0" . $day;
    }
    my $hour = $dt->hour();
    if ($hour < 10) {
        $hour = "0" . $hour;
    }
    $minute = $dt->minute();
    if ($minute < 10) {
        $minute = "0" . $minute;
    }
    my $time_tag = $year . $month . $day;
    my $time_field = $month . "-" . $day . "&nbsp;&nbsp; " . $hour . ":" . $minute;

    return ( $time_tag, $time_field );
}


sub get_oscars_host
{
    my( $input ) = @_;

    my $ipaddr = inet_aton($input);
    my $host = gethostbyaddr($ipaddr, AF_INET);
    if ($host) {
        return($host); 
    }
    else {
        return($input); 
    }
}


# Don't touch the line below
1;
