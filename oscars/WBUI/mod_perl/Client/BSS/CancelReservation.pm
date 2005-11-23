###############################################################################
package Client::BSS::CancelReservation;

# Makes a SOAP call to cancel the reservation listed in the reservation
# details form, and displays the modified reservation results.
#
# Last modified:  November 22, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::BSS::Details;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#______________________________________________________________________________


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'BSS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# output:  prints out the details of the cancelled reservation
# In:   results of SOAP cancel call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    Client::BSS::Details::output_details($results, $self->{session});
} #____________________________________________________________________________


######
1;
