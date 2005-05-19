#!/usr/bin/perl

# userlist.pl:  Admin tool: User List page
# Last modified: May 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use Data::Dumper;

my @fields_to_display = ( 'user_last_name', 'user_first_name', 'user_dn', 'user_level', 'institution_id' );

my (%form_params, %results);

my $cgi = CGI->new();
my ($dn, $user_level, $admin_required) = check_session_status(undef, $cgi);

if (!$error_status) {
    foreach $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    ($error_status, %results) = soap_get_userlist(\%form_params, \@fields_to_display);
    if (!$error_status) {
        update_frames($error_status, "main_frame", "", $results{'status_msg'});
        print_userlist(\%results);
    }
    else {
        update_frames($error_status, "main_frame", "", $results{'error_msg'});
    }
}
else {
    print "Location:  https://oscars.es.net/admin/\n\n";
}

exit;



##### Beginning of sub routines #####

##### sub print_userlist
# In:  results of SOAP call
# Out: None
sub print_userlist
{
    my ( $results ) = @_;
    my ( $rowsref, $row );
	
    $rowsref = $results->{'rows'};
    print "<html>\n";
    print "<head>\n";
    print "<link rel=\"stylesheet\" type=\"text/css\" ";
    print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
    print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
    print "</head>\n\n";

    print "<body onload=\"stripe('userlist', '#fff', '#edf3fe');\">\n\n";

    print "<script language=\"javascript\">print_admin_bar('userlist');</script>\n\n";

    print "<div id=\"zebratable_ui\">\n\n";

    print "<p><em>List of Users</em><br>\n";
    print "<p>Click on the user's last name to view detailed user information.\n";
    print "</p>\n\n";

    print "<table cellspacing=\"0\" width=\"90%\" id=\"userlist\">\n";
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
    foreach $row (@$rowsref) {
        print "  <tr>\n";
        print_row($row);
        print "  </tr>\n";
    }
    print "  </tbody>\n";
    print "</table>\n\n";

    print "</div>\n\n";

    print "<script language=\"javascript\">print_footer();</script>\n";
    print "</body>\n";
    print "</html>\n\n";
}


sub print_row
{
    my( $row ) = @_;

    print '    <td><a href="https://oscars.es.net/cgi-bin/lib/userprofile.pl?id=' . $row->{'user_dn'} . '">' . $row->{'user_last_name'} . '</a></td>' . "\n"; 
    print "    <td>" . $row->{'user_first_name'} . "</td>\n";
    print "    <td>" . $row->{'user_dn'} . "</td>\n";
    print "    <td>" . $row->{'user_level'} . "</td>\n";
    print "    <td>" . $row->{'institution_id'} . "</td>\n";
}
