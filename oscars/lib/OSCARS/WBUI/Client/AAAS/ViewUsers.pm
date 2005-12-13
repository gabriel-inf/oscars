###############################################################################
package Client::AAAS::ViewUsers;

# Handles get user list form submission
#
# Last modified:  December 13, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::UserSession;
use Client::AAAS::Users;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#____________________________________________________________________________ 


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# output:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub output {
    my ( $self, $results ) = @_;

    print $self->{cgi}->header(
         -type=>'text/xml');
    print "<xml>\n";
    print qq{
      <msg>Successfully read user list.</msg>
    };
    Client::AAAS::Users::output_users( $results, $self->{session});
    print "</xml>\n";
}

######
 
######
1;
