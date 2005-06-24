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
        update_status_frame(0, $results->{status_msg});
    }
    else {
        update_status_frame(1, $results->{error_msg});
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

    my( %soap_params, $persistent );

    $soap_params{user_dn} = $form_params{user_dn};
    $soap_params{user_level} = $form_params{user_level};
    $soap_params{reservation_id} = 'NULL';

    # in seconds since epoch
    $soap_params{reservation_start_time} =
                         $form_params->{reservation_start_time};
    # will change which Javascript method sets this up (currently in
    # timeprint.js)
    $soap_params{reservation_tag} =
                         $form_params->{user_dn} . '.' .
                         get_time_str($form_params->{reservation_start_time}) .
                         "-";
    if ($form_params->{duration_hour} eq 'INF') {
        $persistent = 1;
    }
    else {
        $persistent = 0;
    } 
    if (!$persistent) {
        # start time + duration time in seconds
        $soap_params{reservation_end_time} =
                         $form_params->{reservation_start_time} +
                         $form_params->{duration_hour} * 3600;
    }
    else {
        $soap_params{reservation_end_time} = 2 ** 31 - 1;
    }

    $soap_params{reservation_created_time} = '';   # filled in scheduler
    $soap_params{reservation_class} = '4';
    # convert to bps
    $soap_params{reservation_bandwidth} =
                         $form_params->{reservation_bandwidth} * 1000000;
    $soap_params{reservation_src_port} =
                         $form_params->{reservation_src_port};
    $soap_params{reservation_dst_port} =
                         $form_params->{reservation_dst_port};
    $soap_params{reservation_dscp} = $form_params->{reservation_dscp};
    $soap_params{reservation_protocol} = $form_params->{reservation_protocol};
    $soap_params{reservation_burst_limit} = '1000000';
    $soap_params{reservation_status} = 'pending';

    $soap_params{src_address} = $form_params->{src_address};
    $soap_params{dst_address} = $form_params->{dst_address};

    # TODO:  error checking
    if (not_an_ip($soap_params{src_address})) {
        $soap_params{src_address} =
                          inet_ntoa(inet_aton($soap_params{src_address}));
    }

    if (not_an_ip($soap_params{dst_address})) {
        $soap_params{dst_address} =
                          inet_ntoa(inet_aton($soap_params{dst_address}));
    }

    # Undefined fields are set in the PSS.
    if (authorized($form_params->{user_level}, "engr")) {
        if ($form_params{lsp_from}) {
            $soap_params{lsp_from} = $form_params{lsp_from};
        }
        if ($form_params{lsp_to}) {
            $soap_params{lsp_to} = $form_params{lsp_to};
        }
    }
    $soap_params{reservation_description} =
                          $form_params->{reservation_description};
    return( soap_create_reservation(\%soap_params) );
}
######

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $epoch_seconds ) = @_;

    my( $second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year );
    my( $is_DST );
    ($second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year,
              $is_DST) = localtime($epoch_seconds); 
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
