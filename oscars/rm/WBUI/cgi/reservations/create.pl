#!/usr/bin/perl -w

# create.pl:  Called by reservation_form.  Contacts the BSS to
#             create a reservation.
# Last modified: July 1, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use Data::Dumper;
use CGI;

use BSS::Client::SOAPClient;

require '../lib/general.pl';

my( %form_params, $oscars_home );
my $cgi = CGI->new();

($form_params{user_dn}, $form_params{user_level}, $oscars_home) =
                                     check_session_status(undef, $cgi);
if (!$form_params{user_dn}) {
    print "Location:  $oscars_home\n\n";
    exit;
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
process_form(\%form_params);
exit;

######

##############################################################################
# process_form:  Make the SOAP call, and print out the resulting status 
#                message in the status frame
#
sub process_form {
    my( $form_params ) = @_;

    my( $error_status, $results );

    ($error_status, $results) = soap_create_reservation($form_params);
    if (!$error_status) {
        update_status_frame(0, $results->{status_msg});
    }
    else {
        update_status_frame(1, $results->{error_msg});
    }
}
######

######
1;
