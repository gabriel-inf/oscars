#!/usr/bin/perl

# add_form.pl:  Add a user (requires admin privileges)
# Last modified: June 13, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries

require '../lib/general.pl';

my( %form_params );

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}) =
                                         check_session_status(undef, $cgi);

if (!$form_params{user_level}) {
    print "Location:  https://oscars.es.net/admin/\n\n";
    exit;
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
process_form(\%form_params);
exit;

######

###############################################################################
# process_form:  Make the SOAP call, and print out the results
#
sub process_form {
    my( $form_params ) = @_;

    #($error_status, $results) = soap_add_user($form_params);
    my( $error_status, $error_msg );
    if (!$error_status) {
        print_temp($form_params->{user_level});
        update_status_frame(0, "under construction");
    }
    else {
        update_status_frame(1, "under construction");
    }
}
######

sub print_temp {
    my( $user_level ) = @_;

    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '        src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '        src="https://oscars.es.net/timeprint.js"></script>', "\n";
    print '</head>', "\n\n";

    print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n";

    print '<script language="javascript">';
    print '    print_navigation_bar("', $user_level, '", "adduser");';
    print '</script>', "\n";

    print '<h2>Under Construction</h2>', "\n";
    print "<script language=\"javascript\">print_footer();</script>\n";
    print "</body>\n";
    print "</html>\n\n";
}
######


