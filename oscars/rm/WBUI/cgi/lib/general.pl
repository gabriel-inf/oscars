# general.pl
#
# library for general cgi script usage
# Last modified: September 6, 2005
# David Robertson (dwrobertson@lbl.gov)


use CGI;
use Data::Dumper;

use Common::Auth;
use AAAS::Client::SOAPClient;

##############################################################################
# get_params:  Material common to almost all scripts; has to verify
#              user is logged in, and copy over form params
#
sub get_params {

    my( %form_params, $tz, $starting_page );

    my $cgi = CGI->new();
    my $auth = Common::Auth->new();
    ($form_params{user_dn}, $form_params{user_level}, $tz, $starting_page) =
                                             $auth->verify_session($cgi);
    print $cgi->header( -type=>'text/xml' );
    if (!$form_params{user_level}) {
        print "Location:  " . $starting_page . "\n\n";
        return (undef, undef);
    }
    for $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    return( \%form_params, $auth, $starting_page );
}
######

##############################################################################
# get_results:  Material common to almost all scripts;
#               make the SOAP call, and get the results.
#
sub get_results {
    my( $form_params) = @_;

    my $som = aaas_dispatcher($form_params);
    if ($som->faultstring) {
        update_page($som->faultstring);
        return undef;
    }
    my $results = $som->result;
    return $results;
}
######

##############################################################################
# update_page:  If output_func is null, an error has occurred and only the
#               error message is printed in the status div on the OSCARS
#               page.
#
sub update_page {
    my( $msg, $output_func, $user_dn, $user_level) = @_;

    print "<xml>\n";
    print "<msg>\n";
    print "$msg\n";
    print "</msg>\n";
    if ($output_func) {
        print "<user_level>\n";
        print "$user_level\n";
        print "</user_level>\n";
        print "<div>\n";
        $output_func->($user_dn, $user_level);
        print "</div>\n";
    }
    print "</xml>\n";
}
######

##############################################################################
sub output_info {
    my ($unused1, $unused2) = @_;

    print "<div id=\"info_form\"><p>With the advent of service sensitive applications (such as remote",
	  " controlled experiments, time constrained massive data",
        " transfers video-conferencing, etc.), it has become apparent",
        " that there is a need to augment the services present in",
        " today's ESnet infrastructure.</p>\n",

        "<p>Two DOE Office of Science workshops in the past two years have",
        " clearly identified both science discipline driven network",
        " requirements and a roadmap for meeting these requirements.",
        " This project begins to addresses one element of the",
        " roadmap: dynamically provisioned, QoS paths.</p>\n",

        "<p>The focus of the ESnet On-Demand Secure Circuits and",
        " Advance Reservation System (OSCARS) is to develop and",
        " deploy a prototype service that enables on-demand provisioning",
        " of guaranteed bandwidth secure circuits within ESnet.</p>\n",

        "<p>To begin using OSCARS, click on one of the notebook tabs.</p></div>\n";
}
######

##############################################################################
# start_row:  util to print out tr with class depending on input counter
#
# In:  counter
# Out: incremented counter
#
sub start_row {
    my ($ctr) = @_;
   
    if (($ctr % 2) == 0) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    return ($ctr + 1);
}
######


# Don't touch the line below
1;
