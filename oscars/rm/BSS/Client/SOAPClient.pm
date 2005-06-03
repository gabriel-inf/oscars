package BSS::Client::SOAPClient;

use SOAP::Lite;
use Data::Dumper;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( soap_get_reservations soap_create_reservation soap_delete_reservation );
 

my $BSS_server = SOAP::Lite
  -> uri('http://localhost:3000/BSS/Scheduler/ReservationHandler')
  -> proxy ('http://localhost:3000/BSS_server.pl');


##############################################################
# SOAP client calls to BSS front end, invoked from user forms.
##############################################################

##############################################################################
#
sub soap_get_reservations {
    my ($params, $fields_to_read) = @_;
    my $response = $BSS_server->get_reservations($params, $fields_to_read);
    if ($response->fault) {
        print STDERR $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######

##############################################################################
#
sub soap_get_resv_detail {
    my ($params, $fields_to_read) = @_;

    my $response = $BSS_server->get_reservation_detail($params, $fields_to_read);
    if ($response->fault) {
        print STDERR $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######

##############################################################################
#
sub soap_create_reservation {
    my ($params) = @_;
    my $response = $BSS_server->create_reservation($params);
    if ($response->fault) {
        print STDERR $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######

##############################################################################
#
sub soap_delete_reservation {
    my ($params) = @_;
    my $response = $BSS_server->delete_reservation($params);
    if ($response->fault) {
        print STDERR $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######
