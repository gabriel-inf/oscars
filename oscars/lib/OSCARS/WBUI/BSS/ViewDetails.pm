###############################################################################
package OSCARS::WBUI::BSS::ViewDetails;

# Handles request to view a particular reservation's details.
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use OSCARS::WBUI::BSS::Details;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};

#_____________________________________________________________________________ 


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'BSS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# output:  out details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header(
                               -type=>'text/xml');
    OSCARS::WBUI::BSS::Details::output_details(
                              $results, $self->{session}, $self->{user_level});
} #____________________________________________________________________________


######
1;
