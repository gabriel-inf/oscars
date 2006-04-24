#==============================================================================
package OSCARS::WBUI::Method::UserLogin;

=head1 NAME

OSCARS::WBUI::Method::UserLogin - Handles user login.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserLogin;

=head1 DESCRIPTION

Handles user login.  Currently a user name and password are required.  The
user name must be in the OSCARS database for login to succeed.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::UserSession;
use OSCARS::WBUI::Method::Info;

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
# output:  overrides superclass; formats and prints information page
#
sub output {
    my( $self, $som, $soapParams, $authorizations ) = @_;

    my $msg;

    if (!$som) { $msg = "SOAP call $soapParams->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    # if there was an error
    if ($msg) {
        print $self->{cgi}->header( -type=>'text/xml' );
	print "<xml>\n";
        print "<msg>$msg</msg>\n";
        print "</xml>\n";
	return;
    }
    my $results = $som->result;
    my $session = OSCARS::WBUI::UserSession->new();
    my $sid = $session->start($self->{cgi}, $results);
    # for some reason the CGI::Session variant doesn't work
    print $self->{cgi}->header(
	        -type=>'text/xml',
	        -cookie=>$self->{cgi}->cookie(CGISESSID => $sid));
    print "<xml>\n";
    $self->{tabs}->output( 'Info', $authorizations );
    $msg = $self->outputDiv($results, $authorizations);
    print "<msg>$msg</msg>\n";
    print "</xml>\n";
} #___________________________________________________________________________ 


###############################################################################
sub outputDiv {
    my( $self, $results, $authorizations ) = @_;

    my $info = OSCARS::WBUI::Method::Info->new();
    my $msg = $info->outputDiv($results, $authorizations);
    # override in this case
    $msg = "User $results->{login} signed in.\n";
    return $msg;
} #____________________________________________________________________________


######
1;
