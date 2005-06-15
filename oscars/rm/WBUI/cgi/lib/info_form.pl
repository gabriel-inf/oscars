#!/usr/bin/perl -w

# info_form.pl:  Main service: Information page
# Last modified: June 15, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;

require '../lib/general.pl';

my (%form_params);

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}) = check_session_status(undef, $cgi);

if ($form_params{user_dn}) {
    print_info(\%form_params);
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;


sub print_info
{
    my ($form_params) = @_;

    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript" src="https://oscars.es.net/userprofile.js"></script>', "\n";
    print '</head>', "\n\n";

    print '<body>', "\n\n";
    print '<script language="javascript">print_navigation_bar("', $form_params->{user_level}, '", "info");</script>', "\n\n";

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
