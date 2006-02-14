#==============================================================================
package OSCARS::WBUI::UserSession;

=head1 NAME

OSCARS::WBUI::UserSession - Handles user session.

=head1 SYNOPSIS

  use OSCARS::WBUI::UserSession;

=head1 DESCRIPTION

Handles user session.  A cookie is stored in the browser indicating that
the user has logged in already.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 16, 2006

=cut


use strict;

use Data::Dumper;

use CGI qw{:cgi};
use CGI::Session;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #___________________________________________________________________________                                         

###############################################################################
# start_session: Sets cookie containing session id to be used in granting
#   access.  Note that this does not handle checking whether the user is in 
#   the database; that is handled by a method in the AAAS.
#
# In:   ref to CGI instance
# Out:  None
#
sub start_session
{
    my( $self, $cgi, $results ) = @_;

    my $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    my $sid = $session->id();
    my $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param('user_dn', $results->{user_dn});
    $session->param('last_page', $results->{GetInfo});
    return( $sid );
} #____________________________________________________________________________


###############################################################################
# verify_session:  Checks to see that a cookie containing a valid session
# id is set before granting access.
#
# In:  ref to CGI instance
# Out: 1 (logged in)/0 (not logged in)
#
sub verify_session
{
    my( $self, $cgi ) = @_;

    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});

    # Unauthorized user may know to set CGISESSID cookie. However,
    # an entirely new session (without the dn param) will be 
    # created if there is no valid session with that id.
    my $user_dn = $session->param("user_dn");
    if (!$user_dn)  {
        return( undef );
    }
    else {
       $cgi->param(-name=>'user_dn',-value=>$user_dn);
       return( $user_dn );
    }
} #____________________________________________________________________________


###############################################################################
sub end_session
{
    my( $self, $cgi ) = @_;
  
    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    $session->clear(["user_dn"]);
    $session->clear(["last_page"]);
} #____________________________________________________________________________


######
1;
