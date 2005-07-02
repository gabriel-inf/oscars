# general.pl
#
# library for general cgi script usage
# Last modified: July 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;
use Common::Auth;

# To change home directory, change this location
$oscars_home = 'https://oscars.es.net/';

##############################################################################
# check_session_status;  checks CGI session status only; the checks for
# correct password and administrative level are done in the front end to the
# database during initial login.
#
sub check_session_status {
    my( $login_results, $cgi ) = @_;

    my( $auth, $dn, $user_level );

    $auth = Common::Auth->new();
    if ( $login_results ) {
        ($dn, $user_level) = $auth->set_login_status($cgi, $login_results);
        return ($dn, $user_level);
    }
    else {
        ($dn, $user_level) = $auth->verify_login_status($cgi);
        return($dn, $user_level, $oscars_home);
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
# update_status_frame:  Updates message in status frame.
#
# In:  status, and error or status msg
#
sub update_status_frame {
    my ($status, $msg) = @_;

    print '<script language="javascript" type="text/javascript" src="' .
          $oscars_home . 'main_common.js"></script>', "\n";
    print '<script language="javascript" type="text/javascript" src="' .
          $oscars_home . 'timeprint.js"></script>', "\n";
    print '<script language="javascript">update_status_frame("' .
              $status . '", "' . $msg . '");</script>', "\n\n";
}
######

##############################################################################
sub print_info
{
    my ($user_dn, $user_level) = @_;

    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" href="' .
          $oscars_home . 'styleSheets/layout.css">' . "\n";
    print '    <script language="javascript" type="text/javascript" src="' .
               $oscars_home . 'main_common.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript" src="' .
               $oscars_home . 'userprofile.js"></script>', "\n";
    print '</head>', "\n\n";

    print '<body>', "\n\n";
    print '<script language="javascript">print_navigation_bar("', $user_level, '", "info");</script>', "\n\n";

    print '<br/>', "\n";
    print '<p>With the advent of service sensitive applications (such as remote';
    print ' controlled experiments, time constrained massive data', "\n";
    print ' transfers video-conferencing, etc.), it has become apparent';
    print ' that there is a need to augment the services present in';
    print " today's ESnet infrastructure.</p>", "\n";

    print 'Two DOE Office of Science workshops in the past two years have';
    print ' clearly identified both science discipline driven network', "\n";
    print ' requirements and a roadmap for meeting these requirements.';
    print ' This project begins to addresses one element of the', "\n";
    print ' roadmap: dynamically provisioned, QoS paths.</p>';

    print '<p>The focus of the ESnet On-Demand Secure Circuits and';
    print ' Advance Reservation System (OSCARS) is to develop and', "\n";
    print ' deploy a prototype service that enables on-demand provisioning';
    print ' of guaranteed bandwidth secure circuits within ESnet.</p>', "\n";

    print '<hr/>', "\n";

    print '<p>To begin using OSCARS, click on one of the notebook tabs.</p>', "\n";

    print '</body>', "\n";
    print '</html>', "\n";
}
######

##############################################################################
# end_session:  Ends CGI session.
#
sub end_session {
    my( $cgi ) = @_;
    my( $auth );

    $auth = Common::Auth->new();
    $auth->logout($cgi);
}
######

# Don't touch the line below
1;
