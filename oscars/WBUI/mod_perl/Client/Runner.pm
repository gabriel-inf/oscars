package Client::Runner;

# Calls SOAPAdapter to make SOAP call and format results for output.
# Last modified:  November 20, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Apache2::RequestIO;
use CGI qw{:cgi};
use SOAP::Lite;
use Data::Dumper;

use Client::SOAPAdapter;


###############################################################################
#
sub run {

    my ( %soap_params );

    my $soap_server = SOAP::Lite
                          -> uri('http://198.128.14.164/Dispatcher')
                          -> proxy ('https://198.128.14.164/SOAP');
    my $factory = Client::SOAPAdapterFactory->new();
    my $cgi = CGI->new();
    my $adapter = $factory->instantiate($cgi);
    my $user_dn = $adapter->authenticate();
        # TODO:  handle cleanly when not authenticated or authorized
    if (!$user_dn) { return; }
    my $authorized = $adapter->authorize($user_dn);
    if (!$authorized) { return; }
    $adapter->modify_params(\%soap_params);  # adapts from CGI params
    my $results = $adapter->make_call($soap_server, \%soap_params);
    $adapter->post_process($results);
    $adapter->output($results);
}
######

######
1;

