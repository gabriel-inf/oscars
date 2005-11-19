package Client::Runner;

# Calls SOAPAdapter to make SOAP call and format results for output.
# Last modified:  November 18, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use SOAP::Lite;
use Client::SOAPAdapter;


###############################################################################
#
sub run {
    my ( $method_name, %args );

    my $soap_server = SOAP::Lite
                          -> uri('http://198.128.14.164/Dispatcher')
                          -> proxy ('https://198.128.14.164/SOAP');
    my $query = $ENV{'QUERY_STRING'};
    my @pairs = split(';', substr($query, 1));
    for my $pair (@pairs) {
        my ($key, $val) = split('=', $pair) ; 
        if ($key eq 'method') { $method_name=$val; }
        else { $args{$key} = $val; }
    }
    #my $uri = $ENV{'REQUEST_URI'};
    my $factory = Client::SOAPAdapterFactory->new();
    my $adapter = $factory->create_instance($method_name, \%args);
    my $soap_params = $adapter->before_call();
    my $results = $adapter->make_call($soap_server, $soap_params);
    $adapter->after_call($results);
    $adapter->output($results);
}
######

######
1;

