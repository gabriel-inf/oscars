#==============================================================================
package OSCARS::WBUI::Method::UserLogout;

=head1 NAME

OSCARS::WBUI::Method::UserLogout - Handles user logout.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserLogout;

=head1 DESCRIPTION

Handles user logout.  The session cookie for the user is removed.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut


use strict;

use Data::Dumper;
use CGI;
use CGI::Session;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
#
sub output {
    my( $self, $response ) = @_;

    my $cgi = $self->{session}->query();
    $self->{session}->delete();
    $cgi->redirect('/');
} #____________________________________________________________________________


######
1;
