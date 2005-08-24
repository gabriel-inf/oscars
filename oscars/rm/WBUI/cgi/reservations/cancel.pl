#!/usr/bin/perl

# cancel.pl:  Cancel a reservation.
# Last modified: August 24, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;
use Data::Dumper;

use Common::Auth;
use AAAS::Client::SOAPClient;

require '../lib/general.pl';
require 'print_details.pl';

my( %form_params, $tz, $starting_page );

my $cgi = CGI->new();
my $auth = Common::Auth->new();
($form_params{user_dn}, $form_params{user_level}, $tz, $starting_page) =
                                         $auth->verify_session($cgi);
print $cgi->header( -type=>'text/xml' );
if (!$form_params{user_level}) {
    print "Location:  " . $starting_page . "\n\n";
    exit;
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}

$form_params{method} = 'delete_reservation';
my $som = aaas_dispatcher(\%form_params);
if ($som->faultstring) {
    update_page($som->faultstring);
    exit;
}
my $results = $som->result;
print_reservation_detail(\%form_params, $results,
        "Successfully cancelled reservation with id $form_params{reservation_id}.", $auth);
exit;

######
1;
