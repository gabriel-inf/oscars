# general.pl
#
# library for general cgi script usage
# Last modified: August 16, 2005
# David Robertson (dwrobertson@lbl.gov)


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

    print "<p>With the advent of service sensitive applications (such as remote",
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

        "<p>To begin using OSCARS, click on one of the notebook tabs.</p>\n";
}
######

# Don't touch the line below
1;
