###############################################################################
package OSCARS::WBUI::AAAS::Logout;

# Handles user logout.
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;
use CGI;

use OSCARS::WBUI::UserSession;

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

    print $self->{cgi}->redirect('/');
} #____________________________________________________________________________ 

######
1;
