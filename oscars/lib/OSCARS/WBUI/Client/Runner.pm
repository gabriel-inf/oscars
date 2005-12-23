###############################################################################
package Client::Runner;

# Calls SOAPAdapter to make SOAP call and format results for output.
# Last modified:  December 22, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

# TODO:  check security implications
use lib qw(/usr/local/esnet/servers/prod);

use CGI qw{:cgi};
use SOAP::Lite;
use Data::Dumper;

use Client::SOAPAdapter;

#______________________________________________________________________________


###############################################################################
#
sub run {
    my ( %soap_params );

    my $soap_server = SOAP::Lite
                          -> uri('http://localhost:2000/OSCARS/Dispatcher')
                          -> proxy('http://localhost:2000/Server');
    my $factory = Client::SOAPAdapterFactory->new();
    my $cgi = CGI->new();
    my $adapter = $factory->instantiate($cgi);
    $adapter->handle_request($soap_server);
} #____________________________________________________________________________

######
1;

