#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: October 31, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

require 'general.pl';


my $cgi = CGI->new();
my ($user_dn, $user_level, $unused, $starting_page) = verify_session($cgi);

# logout user from resource manager
my ($som) = logout_user($user_dn);

# nuke session and put the user back at the login screen

if ($user_dn) { end_session($cgi); }

print "Location:  https://oscars.es.net/\n\n";
exit;

##############################################################################
# logout_user:  Closes db connections to AAAS (which closes them in BSS)
# In:  user email address
# Out: error status, SOAP results
#
sub logout_user {
    my( $user_dn ) = @_;

    my( %soap_params );

    $soap_params{user_dn} = $user_dn;
    $soap_params{method} = 'logout';
    my $som = aaas_dispatcher(\%soap_params);
    return( $som );
}
######
