#==============================================================================
package OSCARS::WBUI::AAAS::Login;

=head1 NAME

OSCARS::WBUI::AAAS::Login - Handles user login.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::Login;

=head1 DESCRIPTION

Handles user login.  Currently a user name and password are required.  The
user name must be in the OSCARS database for login to succeed.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 28, 2006

=cut


use strict;

use Data::Dumper;
use CGI qw{:cgi};

use OSCARS::WBUI::UserSession;
use OSCARS::WBUI::NavigationBar;
use OSCARS::WBUI::Info;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# Overrides super-class call to avoid trying to verify a non-existent session.
# In this case, the SOAP call is used to authenticate.
#
sub authenticate {
    my( $self ) = @_;

    return 1;
} #____________________________________________________________________________


###############################################################################
# overrides super-class call
#
sub post_process {
    my( $self, $results ) = @_;

    $self->{user_dn} = $results->{user_dn};
    my $sid = $self->{session}->start_session($self->{cgi}, $results);
    print $self->{cgi}->header(
         -type=>'text/xml',
         -cookie=>$self->{cgi}->cookie(CGISESSID => $sid));
} #____________________________________________________________________________


###############################################################################
sub output {
    my( $self, $results ) = @_;

    my $info = OSCARS::WBUI::Info->new();
    print "<xml>\n";
    print "<msg>User $self->{user_dn} signed in.</msg>\n";
    $self->{tabs}->output('Info', $results->{authorizations});
    $info->output($results);
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
