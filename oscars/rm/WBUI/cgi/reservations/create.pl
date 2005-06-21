#!/usr/bin/perl -w

# create.pl:  Called by reservation_form.  Contacts the BSS to
#             create a reservation.
# Last modified: June 17, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use Socket;
use Data::Dumper;
use CGI;

use BSS::Client::SOAPClient;

require '../lib/general.pl';

my (%form_params);
my $cgi = CGI->new();

($form_params{user_dn}, $form_params{user_level}) =
                                     check_session_status(undef, $cgi);
if (!$form_params{user_dn}) {
    print "Location:  https://oscars.es.net/\n\n";
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

    ($error_status, $results) = create_reservation($form_params);
    if (!$error_status) {
        update_frames(0, "success", "status_frame", "", $results->{status_msg});
    }
    else {
        update_frames(0, "error", "status_frame", "", $results->{error_msg});
    }
}
######

##############################################################################
# create_reservation:  attempt to create a new reservation, and print out
#                      the result
# In:  form parameters
# Out: None
sub create_reservation {
    my( $form_params ) = @_;

    my( %soap_params );

    $soap_params{user_dn} = $form_params{user_dn};
    $soap_params{user_level} = $form_params{user_level};
    $soap_params{reservation_id} = 'NULL';

    # in seconds since epoch
    $soap_params{reservation_start_time} = $form_params->{reservation_start_time};
    # will change which Javascript method sets this up (currently in timeprint.js)
    $soap_params{reservation_tag} = $form_params->{user_dn} . '.' . get_time_str($form_params->{reservation_start_time}) . "-";
    
    # start time + duration time in seconds
    $soap_params{reservation_end_time} = $form_params->{reservation_start_time} + $form_params->{duration_hour} * 3600;

    $soap_params{reservation_created_time} = '';   # filled in scheduler
    $soap_params{reservation_bandwidth} = $form_params->{reservation_bandwidth} . 'm';
    $soap_params{reservation_class} = '4';
    $soap_params{reservation_ingress_port} = $form_params->{reservation_ingress_port};
    $soap_params{reservation_egress_port} = $form_params->{reservation_egress_port};
    $soap_params{reservation_dscp} = $form_params->{reservation_dscp};
    $soap_params{protocol} = $form_params->{protocol};
    $soap_params{reservation_burst_limit} = '1m';
    $soap_params{reservation_status} = 'pending';

    $soap_params{src_hostaddrs_ip} = $form_params->{origin};
    $soap_params{dst_hostaddrs_ip} = $form_params->{destination};

    # TODO:  error checking
    if (not_an_ip($soap_params{src_hostaddrs_ip})) {
        $soap_params{src_hostaddrs_ip} = inet_ntoa(inet_aton($soap_params{src_hostaddrs_ip}));
    }

    if (not_an_ip($soap_params{dst_hostaddrs_ip})) {
        $soap_params{dst_hostaddrs_ip} = inet_ntoa(inet_aton($soap_params{dst_hostaddrs_ip}));
    }

    # Undefined fields are set in the PSS.
    if (authorized($form_params->{user_level}, "engr")) {
        if ($form_params{lsp_from}) {
            $soap_params{lsp_from} = $form_params{lsp_from};
        }
        if ($form_params{lsp_to}) {
            $soap_params{lsp_to} = $form_params{lsp_to};
        }
        if (defined($form_params{persistent})) {
            $soap_params{persistent} = $form_params{persistent};
        }
    }
    $soap_params{reservation_description} =  $form_params->{reservation_description};
    return( soap_create_reservation(\%soap_params) );
}
######

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $epoch_seconds ) = @_;

    my( $second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year, $is_DST);
    ($second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year, $is_DST) = localtime($epoch_seconds); 
    $year += 1900;
    $month += 1;
    if ($month < 10) {
        $month = "0" . $month;
    }
    if ($day < 10) {
        $day = "0" . $day;
    }
    if ($hour < 10) {
        $hour = "0" . $hour;
    }
    if ($minute < 10) {
        $minute = "0" . $minute;
    }
    my $time_tag = $year . $month . $day;

    return( $time_tag );
}
######

##############################################################################
# not_an_ip:  check whether a host name or an IP address has been given
sub not_an_ip
{
    my($string) = @_;

    return($string !~ /^([\d]+)\.([\d]+)\.([\d]+)\.([\d]+)$/);
}
######
