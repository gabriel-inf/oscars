# general.pl
#
# library for general cgi script usage
# Last modified: June 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;
use Socket;

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;


##############################################################################
# check_session_status;  checks CGI session status only; the checks for
# correct password and administrative level are done in the front end to the
# database during initial login.
#
sub check_session_status {
    my( $login_results, $cgi ) = @_;

    my( $auth, $dn, $user_level );

    $auth = AAAS::Client::Auth->new();
    if ( $login_results ) {
        ($dn, $user_level) = $auth->set_login_status($cgi, $login_results);
        return ($dn, $user_level);
    }
    else {
        ($dn, $user_level) = $auth->verify_login_status($cgi);
        return($dn, $user_level);
    }
}
######

##############################################################################
# authorized:  Given the user level string, see if the user has the required
#              privilege 
#
sub authorized {
    my( $user_level, $required_priv ) = @_;
 
    for my $priv (split(' ', $user_level)) {
        if ($priv eq $required_priv) {
            return( 1 );
        }
    }
    return( 0 );
}
######

##############################################################################
# update_frames:  Prints to the status frame, and sets location of the main
# frame if a URI is given.
#
# In:  uri, error or status msg
#
sub update_frames {
    my ($print_type_header, $status, $target, $uri, $msg) = @_;

    if ($print_type_header) { print 'Content-type: text/html', "\n\n"; }
    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '<script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '<script language="javascript" type="text/javascript" src="https://oscars.es.net/timeprint.js"></script>', "\n";
    print '</head>', "\n";
    print '<body>', "\n";
    print "<script language=\"javascript\">update_status_message(\"$target\", \"$msg\", \"$status\");</script>";
    if ($uri) {
        print "<script language=\"javascript\">update_frame(\"main_frame\", \"$uri\");</script>";
    }
    print '</body>', "\n";
    print '</html>', "\n";
    print "\n\n";
}
######

##############################################################################
# end_session:  Ends CGI session.
#
sub end_session {
    my( $cgi ) = @_;
    my( $auth );

    $auth = AAAS::Client::Auth->new();
    $auth->logout($cgi);
}
######

##############################################################################
# get_oscars_host:  finds host name from IP address if possible
#
sub get_oscars_host {
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
######

# Don't touch the line below
1;
