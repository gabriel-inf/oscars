###############################################################################
package Client::Runner;

# Calls SOAPAdapter to make SOAP call and format results for output.
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use CGI qw{:cgi};
use SOAP::Lite;
use Data::Dumper;

use Client::SOAPAdapter;

#______________________________________________________________________________


###############################################################################
#
sub run {
    my ( %soap_params );

    # TODO: fix URL
    my $soap_server = SOAP::Lite
                          -> uri('http://198.128.14.164/Dispatcher')
                          -> proxy ('https://198.128.14.164/SOAP');
    my $factory = Client::SOAPAdapterFactory->new();
    my $cgi = CGI->new();
    my $adapter = $factory->instantiate($cgi);
    $adapter->handle_request($soap_server);
} #____________________________________________________________________________

######
1;

