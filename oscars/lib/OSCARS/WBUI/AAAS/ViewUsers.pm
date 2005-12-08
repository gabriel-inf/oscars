###############################################################################
package OSCARS::WBUI::AAAS::ViewUsers;

# Handles get user list form submission
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use OSCARS::WBUI::UserSession;
use OSCARS::WBUI::AAAS::Users;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};

#____________________________________________________________________________ 


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'AAAS';
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
    OSCARS::WBUI::AAAS::Users::output_users( $results, $self->{session});
    print "</xml>\n";
}

######
 
######
1;
