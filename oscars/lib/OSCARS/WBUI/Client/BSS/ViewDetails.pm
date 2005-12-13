###############################################################################
package Client::BSS::ViewDetails;

# Handles request to view a particular reservation's details.
#
# Last modified:  December 13, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::BSS::Details;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#_____________________________________________________________________________ 


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

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
    Client::BSS::Details::output_details(
                              $results, $self->{session}, $self->{user_level});
} #____________________________________________________________________________


######
1;
