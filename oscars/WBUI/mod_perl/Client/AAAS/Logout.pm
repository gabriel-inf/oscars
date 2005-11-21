###############################################################################
package Client::AAAS::Logout;

# Handles user login.
#
# Last modified:  November 20, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;
use CGI;

use Client::UserSession;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#____________________________________________________________________________ 


###############################################################################
# Currently a noop.
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    return {};
} #____________________________________________________________________________ 


###############################################################################
# post_process:  In this case, closes CGI session.
#
sub post_process {
    my( $self, $results ) = @_;

    $self->{session}->end_session($self->{cgi});
    return {};
} #___________________________________________________________________________                                         


###############################################################################
#
sub output {
    my( $self, $results ) = @_;

    # TODO:  FIX hard-coded URL
    print $self->{cgi}->redirect('https://oscars-test.es.net/');
} #____________________________________________________________________________ 

######
1;
