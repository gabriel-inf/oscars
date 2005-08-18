#!/usr/bin/perl

# userlist_form.pl:  User List page
# Last modified: August 16, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;
use Data::Dumper;

use Common::Auth;
use AAAS::Client::SOAPClient;

require '../lib/general.pl';


my( %form_params, $tz, $starting_page );

my $cgi = CGI->new();
my $auth = Common::Auth->new();
($form_params{user_dn}, $form_params{user_level}, $tz, $starting_page) =
                                         $auth->verify_session($cgi);
print $cgi->header( -type=>'text/xml' );
if (!$form_params{user_level}) {
    print "Location:  $starting_page\n\n";
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

    $form_params{method} = 'get_userlist';
    my $som = aaas_dispatcher($form_params);
    if ($som->faultstring) {
        print $cgi->header( -type=>'text/xml' );
        update_page($som->faultstring);
        return;
    }
    my $results = $som->result;
    print "<xml>";
    print "<msg>Successfully read user list</msg>\n";
    print "<div id=\"zebratable_ui\">";
    print_userlist($results);
    print "</div>";
    print "</xml>\n";
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
    my $even = 0;

    $rowsref = $results->{rows};
    print "<p>Click on the user's last name to view detailed user information.</p>";
    print "<table cellspacing=\"0\" width=\"90%\" class=\"sortable\" id=\"userlist\">";
    print "<thead><tr>\n";
    print "   <td>Last Name</td>";
    print "   <td>First Name</td>";
    print "   <td>Distinguished Name</td>";
    print "   <td>Level</td>";
    print "   <td>Organization</td>";
    print "   <td>Status</td>";
    print "</tr></thead>\n";

    print "<tbody>\n";
    for $row (@$rowsref) {
        if ($even) {
            print " <tr class=\"even\">";
        }
        else {
            print " <tr class=\"odd\">";
        }
        print_row($row);
        print " </tr>\n";
        $even = !$even;
    }
    print "</tbody></table>\n";
}
######

###############################################################################
# print_row:  print the information for one user
#
sub print_row
{
    my( $row ) = @_;

    print "<td><a href=\"#\" style=\"$starting_page/test/styleSheets/layout.css\"";
    print " onclick=\"new_page";
    print "('profile_form', '$starting_page/cgi-bin/test/users/profile_form.pl?id=$row->{user_dn}'",
        ");return false;\">$row->{user_last_name}</a></td>\n";
    print "<td>$row->{user_first_name}</td>";
    print "<td>$row->{user_dn}</td>";
    print "<td>$row->{user_level}</td>";
    print "<td>$row->{institution_id}</td>";
    print "<td>$row->{user_status}</td>";
}
######
