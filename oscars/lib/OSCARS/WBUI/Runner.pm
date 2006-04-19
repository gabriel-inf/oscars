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
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use CGI qw{:cgi};
use SOAP::Lite;
use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;

#______________________________________________________________________________


###############################################################################
#
sub run {
    my $soapURI = 'http://localhost:2000/OSCARS/Dispatcher';
    my $soapProxy = 'http://localhost:2000/Server';
    my $cgi = CGI->new();
    my $soapServer = SOAP::Lite
                          -> uri( $soapURI )
                          -> proxy( $soapProxy );
    my $factory = OSCARS::WBUI::SOAPAdapterFactory->new();
    my $adapter = $factory->instantiate($cgi);
    $adapter->handleRequest($soapServer);
} #____________________________________________________________________________


######
1;

