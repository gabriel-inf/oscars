package Client::Runner;

# Calls SOAPAdapter to make SOAP call and format results for output.
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use SOAP::Lite;
use Client::SOAPAdapter;


###############################################################################
#
sub run {
    my $soap_server = SOAP::Lite
                          -> uri('http://198.128.14.164/Dispatcher')
                          -> proxy ('https://198.128.14.164/SOAP');
    my $adapter = Client::SOAPAdapterFactory->new($ENV{'REQUEST_URI'},
                                                  $ENV{'QUERY_STRING'});
    my $soap_params = $adapter->pre_call();
    my $results = $adapter->make_call($soap_server, $soap_params);
    $adapter->post_call($results);
    $adapter->output($results);
}
######

######
1;

