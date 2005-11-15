#!/usr/bin/perl -w

# adapt.pl: Script called by all CGI forms.  Client::SOAPAdapter
#           does all the work.
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use Client::SOAPAdapter;

# TODO:  how to instantiate this only once and reuse
#        (taking out "my" doesn't do the trick)
my $soap_server = SOAP::Lite
                 -> uri('http://198.128.14.164/Dispatcher')
                 -> proxy ('https://198.128.14.164/SOAP');
my $adapter = Client::SOAPAdapter->new();
my $results = $adapter->make_soap_call($soap_server);
$adapter->output($results);
exit;

######
1;
