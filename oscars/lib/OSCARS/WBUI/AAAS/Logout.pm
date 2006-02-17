#==============================================================================
package OSCARS::WBUI::AAAS::Logout;

=head1 NAME

OSCARS::WBUI::AAAS::Logout - Handles user logout.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::Logout;

=head1 DESCRIPTION

Handles user logout.  The session cookie for the user is removed.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 16, 2006

=cut


use strict;

use Data::Dumper;
use CGI;

use OSCARS::WBUI::UserSession;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


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
