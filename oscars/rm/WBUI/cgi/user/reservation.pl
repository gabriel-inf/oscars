#!/usr/bin/perl -w

# reservation.pl:  Main interface CGI program for network resource
#                  reservation process
# Last modified: May 18, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use DateTime;
use Socket;
use Data::Dumper;
use CGI;

use BSS::Client::SOAPClient;

require '../lib/general.pl';


my $cgi = CGI->new();

my (%form_params, %results);

my ($dn, $user_level, $admin_required) = check_session_status(undef, $cgi);

if ($dn) {
    foreach $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    ($error_status, %results) = create_reservation($dn, \%form_params);
    if (!$error_status) {
        update_frames($error_status, "status_frame", "", $results{'status_msg'});
    }
    else {
        update_frames($error_status, "status_frame", "", $results{'error_msg'});
    }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;


##### sub create_reservation
# In: None
# Out: None
sub create_reservation
{
    my( $dn, $form_params ) = @_;
    my( %soap_params );
    my( %results);

    $soap_params{'reservation_id'} =              'NULL';
    $soap_params{'user_dn'} =             $dn;

    # in seconds since epoch
    $soap_params{'reservation_start_time'} =     $form_params->{'reservation_start_time'};
    # start time + duration time in seconds
    $soap_params{'reservation_end_time'} =       $form_params->{'reservation_start_time'} + $form_params->{'duration_hour'} * 3600;

    $soap_params{'reservation_created_time'} =   '';   # filled in scheduler
    $soap_params{'reservation_bandwidth'} =      $form_params->{'reservation_bandwidth'} . 'm';
    $soap_params{'reservation_class'} =          '4';
    $soap_params{'reservation_burst_limit'} =    '1m';
    $soap_params{'reservation_status'} =         'pending';

    $soap_params{'ingress_interface_id'}= '';   # db lookup in scheduler
    $soap_params{'egress_interface_id'}=  '';   # db lookup in scheduler

    $soap_params{'src_hostaddrs_ip'} =         $form_params->{'origin'};
    $soap_params{'dst_hostaddrs_ip'} =         $form_params->{'destination'};

    # TODO:  error checking
    if (not_an_ip($soap_params{'src_hostaddrs_ip'})) {
        $soap_params{'src_hostaddrs_ip'} = inet_ntoa(inet_aton($soap_params{'src_hostaddrs_ip'}));
    }

    if (not_an_ip($soap_params{'dst_hostaddrs_ip'})) {
        $soap_params{'dst_hostaddrs_ip'} = inet_ntoa(inet_aton($soap_params{'dst_hostaddrs_ip'}));
    }

    $soap_params{'reservation_ingress_port'} =   '';     # db lookup in schedule
    $soap_params{'reservation_egress_port'} =    '';     # db lookup in scheduler

    $soap_params{'lsp_from'} =    '';        # done in PSS
    $soap_params{'lsp_to'} =      '';        # done in PSS
    $soap_params{'reservation_description'} =    $form_params->{'reservation_description'};
    return( soap_create_reservation(\%soap_params) );
}


sub not_an_ip
{
    my($string) = @_;

    return($string !~ /^([\d]+)\.([\d]+)\.([\d]+)\.([\d]+)$/);
}
