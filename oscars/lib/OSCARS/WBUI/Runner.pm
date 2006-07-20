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

July 19, 2006

=cut


use strict;

use OSCARS::WBUI::SOAPAdapter;

#______________________________________________________________________________


###############################################################################
#
sub run {
    my $factory = OSCARS::WBUI::SOAPAdapterFactory->new();
    my $adapter = $factory->instantiate();
    $adapter->handleRequest();
} #____________________________________________________________________________


######
1;

