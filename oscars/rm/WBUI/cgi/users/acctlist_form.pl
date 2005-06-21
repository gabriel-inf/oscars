#!/usr/bin/perl

# userlist_form.pl:  User List page
# Last modified: June 13, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use Data::Dumper;

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

    my( $error_status, $results);

    ($error_status, $results) = soap_get_userlist($form_params);
    if (!$error_status) {
        update_frames($error_status, "success", "main_frame", "", $results->{status_msg});
        print_userlist($results);
    }
    else {
        update_frames($error_status, "error", "main_frame", "", $results->{error_msg});
    }
}
######

###############################################################################
# print_userlist:  If the caller has admin privileges print a list of all users
#                  returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub print_userlist
{
    my ( $results ) = @_;
    my ( $rowsref, $row );
	
    $rowsref = $results->{rows};
    print "<html>\n";
    print "<head>\n";
    print '<link rel="stylesheet" type=\"text/css" ';
    print ' href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript" src="https://oscars.es.net/sorttable.js"></script>', "\n";
    print "</head>\n\n";

    print "<body onload=\"stripe('userlist', '#fff', '#edf3fe');\">\n\n";

    print "<script language=\"javascript\">print_navigation_bar('admin', 'userlist');</script>\n\n";

    print "<div id=\"zebratable_ui\">\n\n";

    print "<p>Click on the user's last name to view detailed user information.\n";
    print "</p>\n\n";

    print '<table cellspacing="0" width="90%" class="sortable" id="userlist">', "\n";
    print "  <thead>\n";
    print "  <tr>\n";
    print "    <td >Last Name</td>\n";
    print "    <td >First Name</td>\n";
    print "    <td >Distinguished Name</td>\n";
    print "    <td >Level</td>\n";
    print "    <td >Organization</td>\n";
    print "  </tr>\n";
    print "  </thead>\n";

    print "  <tbody>\n";
    for $row (@$rowsref) {
        print "  <tr>\n";
        print_row($row);
        print "  </tr>\n";
    }
    print "  </tbody>\n";
    print "</table>\n\n";

    print "</div>\n\n";

    print '<script language="javascript">print_footer();</script>', "\n";
    print "</body>\n";
    print "</html>\n\n";
}
######

###############################################################################
# print_row:  print the information for one user
#
sub print_row
{
    my( $row ) = @_;

    print '    <td><a href="https://oscars.es.net/cgi-bin/users/profile_form.pl?id=' . $row->{user_dn} . '">' . $row->{user_last_name} . '</a></td>' . "\n"; 
    print "    <td>" . $row->{user_first_name} . "</td>\n";
    print "    <td>" . $row->{user_dn} . "</td>\n";
    print "    <td>" . $row->{user_level} . "</td>\n";
    print "    <td>" . $row->{institution_id} . "</td>\n";
}
######
