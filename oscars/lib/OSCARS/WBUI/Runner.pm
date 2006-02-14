#==============================================================================
package OSCARS::WBUI::Runner;

=head1 NAME

OSCARS::WBUI::Runner - Makes SOAP call and formats results for output.

=head1 SYNOPSIS

  use OSCARS::WBUI::Runner;

=head1 DESCRIPTION

Uses SOAPAdapterFactory to create a SOAP method instance, makes the SOAP call,
and formats the results for output.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 10, 2006

=cut


use strict;

# TODO:  check security implications
use lib qw(/usr/local/esnet/servers/prod);

use CGI qw{:cgi};
use SOAP::Lite;
use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;

#______________________________________________________________________________


###############################################################################
#
sub run {
    my ( %soap_params );

    my $soap_server = SOAP::Lite
                          -> uri('http://localhost:2000/OSCARS/Dispatcher')
                          -> proxy('http://localhost:2000/Server');
    my $factory = OSCARS::WBUI::SOAPAdapterFactory->new();
    my $cgi = CGI->new();
    my $adapter = $factory->instantiate($cgi);
    $adapter->handle_request($soap_server);
} #____________________________________________________________________________

######
1;

