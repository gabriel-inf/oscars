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

July 19, 2006

=cut


use strict;

use CGI::Session;
use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# Overrides super-class call to avoid trying to load an existing session.
# In this case, the SOAP call is used to authenticate.
#
sub authenticate {
    my( $self ) = @_;

    $self->{session} = CGI::Session->new() or die CGI::Session->errstr;
    $self->{session}->expire("+8h");  # expire after 8 hours
    return 1;
} #____________________________________________________________________________


###############################################################################
# output:  overrides superclass; formats and prints information page
#
sub output {
    my( $self, $som, $request ) = @_;

    my $msg;

    if (!$som) { $msg = "SOAP call $self->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    # if there was an error
    if ($msg) {
        print $self->{session}->header( -type=>'text/xml' );
	print "<xml>\n";
        print "<status>$msg</status>\n";
        print "</xml>\n";
	$self->{session}->delete();
	return;
    }
    my $response = $som->result;
    # set parameters now that have successfully authenticated via SOAP call
    $self->{session}->param("login", $request->{login});
    # needs the space
    $self->{session}->param("tab", " ");

    my $tabs = OSCARS::WBUI::NavigationBar->new();
    print $self->{session}->header( -type=>'text/xml' );
    print "<xml>\n";
    # output status
    print "<status>User $request->{login} signed in.</status>\n";
    # initialize navigation bar
    $tabs->init( $response->{tabs} );
    print "<content><h3>Click on a tab to start using the system</h3></content>\n";
    # clear information section
    print "<info> </info>\n";
    print "</xml>\n";
} #___________________________________________________________________________ 


######
1;
