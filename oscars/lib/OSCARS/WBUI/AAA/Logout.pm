#==============================================================================
package OSCARS::WBUI::AAA::Logout;

=head1 NAME

OSCARS::WBUI::AAA::Logout - Handles user logout.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAA::Logout;

=head1 DESCRIPTION

Handles user logout.  The session cookie for the user is removed.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;
use CGI;

use OSCARS::WBUI::UserSession;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# postProcess:  In this case, closes CGI session.
#
sub postProcess {
    my( $self, $params, $results ) = @_;

    my $session = OSCARS::WBUI::UserSession->new();
    $session->end($self->{cgi});
    return {};
} #___________________________________________________________________________ 


###############################################################################
#
sub output {
    my( $self, $results, $authorizations ) = @_;

    $self->{cgi}->redirect('/');
} #____________________________________________________________________________


######
1;
