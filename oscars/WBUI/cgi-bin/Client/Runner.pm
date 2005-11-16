package Client::Runner;

# Runner:  Calls SOAPAdapter to make SOAP call and format results for output
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Error qw(:try);
use Data::Dumper;

use SOAP::Lite;
use Client::SOAPAdapter;


###############################################################################
#
sub run {
    my $soap_server = SOAP::Lite
                          -> uri('http://198.128.14.164/Dispatcher')
                          -> proxy ('https://198.128.14.164/SOAP');
    my $adapter = Client::SOAPAdapter->new();
    my $results = $adapter->make_soap_call($soap_server);
    $adapter->output($results);
}
######

######
1;

