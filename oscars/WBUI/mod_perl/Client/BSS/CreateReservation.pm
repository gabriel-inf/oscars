###############################################################################
package Client::BSS::CreateReservation;

# Handles displaying the details of a reservation that has just been made.
#
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#______________________________________________________________________________ 


###############################################################################
# output:  TODO:  need to print out reservation details
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    my $params_str;

    print $self->{cgi}->header(
         -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>Successfully created reservation</msg>
    <div id="reservation_ui">
    <p>TODO</p>
    </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________ 


######
1;
