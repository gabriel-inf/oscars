package AAAS::Client::SOAPClient;

use SOAP::Lite;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( soap_verify_login soap_check_login soap_logout soap_get_profile soap_set_profile soap_get_userlist);


######################################
# All calls are made to AAAS front end
######################################

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAAS/Frontend/User')
  -> proxy ('http://localhost:2000/AAAS_server.pl');



################################
# Methods called from user forms
################################

##############################################################################
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
######

##############################################################################
#
sub soap_get_profile {
    my ( $params ) = @_;

    my $response = $AAAS_server->get_profile($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######

##############################################################################
#
sub soap_set_profile {
    my ( $params ) = @_;

    my $response = $AAAS_server->set_profile($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######

##############################################################################
#
sub soap_logout {
    my ( $params ) = @_;
  
    my $response = $AAAS_server->logout($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
######
    

#################################
# Methods called from admin forms
#################################

##############################################################################
#
sub soap_get_userlist
{
    my ( $params ) = @_;
    my $response = $AAAS_server->get_userlist($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}
######

################################
# Methods called from BSS 
################################

##############################################################################
#
sub soap_check_login
{
    my ($params) = @_;
    my $response = $AAAS_server->check_login_status($params);
    if ($response->fault) {
        print STDERR $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}
######

#####
1;
