#!/usr/bin/perl -w

# create.pl:  Called by reservation_form.  Contacts the BSS to
#             create a reservation.
# Last modified: June 10, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use Socket;
use DateTime;
use Data::Dumper;
use CGI;

use BSS::Client::SOAPClient;

require '../lib/general.pl';


my $cgi = CGI->new();

my (%form_params, %results);

my ($dn, $user_level) = check_session_status(undef, $cgi);

if ($dn) {
    for $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    $form_params{user_level} = $user_level;
    ($error_status, %results) = create_reservation($dn, \%form_params);
    if (!$error_status) {
        update_frames(0, "status_frame", "", $results{status_msg});
    }
    else {
        update_frames(0, "status_frame", "", $results{error_msg});
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

    $soap_params{reservation_id} = 'NULL';
    $soap_params{user_dn} = $dn;

    # in seconds since epoch
    $soap_params{reservation_start_time} = $form_params->{reservation_start_time};
    # will change which Javascript method sets this up (currently in timeprint.js)
    $soap_params{reservation_tag} = $form_params->{user_dn} . '.' . get_time_str($form_params->{reservation_start_time}) . "-";
    
    # start time + duration time in seconds
    $soap_params{reservation_end_time} = $form_params->{reservation_start_time} + $form_params->{duration_hour} * 3600;

    $soap_params{reservation_created_time} = '';   # filled in scheduler
    $soap_params{reservation_bandwidth} = $form_params->{reservation_bandwidth} . 'm';
    $soap_params{reservation_class} = '4';
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


###############################################################################
sub get_time_str {
    my( $epoch_seconds ) = @_;

    my $dt = DateTime->from_epoch( epoch => $epoch_seconds );
    my $year = $dt->year();
    if ($year < 10) {
        $year = "0" . $year;
    }
    my $month = $dt->month();
    if ($month < 10) {
        $month = "0" . $month;
    }
    my $day = $dt->day();
    if ($day < 10) {
        $day = "0" . $day;
    }
    my $hour = $dt->hour();
    if ($hour < 10) {
        $hour = "0" . $hour;
    }
    my $minute = $dt->minute();
    if ($minute < 10) {
        $minute = "0" . $minute;
    }
    my $time_tag = $year . $month . $day;

    return( $time_tag );
}
######

sub not_an_ip
{
    my($string) = @_;

    return($string !~ /^([\d]+)\.([\d]+)\.([\d]+)\.([\d]+)$/);
}
