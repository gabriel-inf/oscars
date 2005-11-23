###############################################################################
package Client::BSS::ViewDetails;

# Handles request to view a given set of reservations, or a particular
# reservation's details.
#
# Last modified:  November 22, 2005
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

    $params->{server_name} = 'BSS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# output:  print details of reservation created by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    Client::BSS::Details::output_details($results, $self->{session});
} #____________________________________________________________________________


######
1;
