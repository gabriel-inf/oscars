package AAAS::Client::SOAPClient;

use SOAP::Lite;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( soap_verify_login soap_logout soap_get_profile soap_set_profile soap_get_userlist);


#####
## calls to AAAS front end
#####

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAAS/Frontend/User')
  -> proxy ('http://localhost:2000/AAAS_server.pl');

# TODO:  one SOAP call that dispatches according to server, subroutine args


#
# methods called from user forms
#

sub soap_verify_login
{
    my ($params) = @_;
    my $response = $AAAS_server->verify_login($params);
    if ($response->fault) {
        print STDERR $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}

sub soap_get_profile
{
    my ($params, $fields_to_display ) = @_;
    my $response = $AAAS_server->get_profile($params, $fields_to_display);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_set_profile
{
    my ($params, $fields_to_read) = @_;
    my $response = $AAAS_server->set_profile($params, $fields_to_read);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_logout
{
    my $response = $AAAS_server->logout();
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
    

###
# methods called from admin forms
###

sub soap_get_userlist
{
    my ($params, $fields_to_display ) = @_;
    my $response = $AAAS_server->get_userlist($params, $fields_to_display);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}


