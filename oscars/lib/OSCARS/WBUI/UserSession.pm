#==============================================================================
package OSCARS::WBUI::UserSession;

=head1 NAME

OSCARS::WBUI::UserSession - Handles user session.

=head1 SYNOPSIS

  use OSCARS::WBUI::UserSession;

=head1 DESCRIPTION

Handles user session.  A cookie is stored in the browser indicating that
the user has logged in already.  Note that this instance is not meant to
be persistent, a new CGI::Session is created for each SOAP call.  The
Login method calls start and the Logout method calls end.  Every other
SOAP call calls verify.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 19, 2006

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
# start: Sets session parameters.  Used by verify to make sure that browser
#   request has same session id, and has a user_login parameter set.
#   Note that this does not handle checking whether the user is in 
#   the database; that is handled by a method in the AAAS prior to calling
#   this method.
#
# In:   ref to CGI instance, results of database user/password check
# Out:  None
#
sub start
{
    my( $self, $cgi, $results ) = @_;

    my $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    my $sid = $session->id();
    $session->param('user_login', $results->{user_login});
    $session->param('last_page', $results->{GetInfo});
    $session->expire('+8h');  # expire after 8 hours
    return $sid;
} #____________________________________________________________________________


###############################################################################
# verify:  Checks to see if there has been a session created before
# granting access.

# In:  ref to CGI instance
# Out: 1 (logged in)/0 (not logged in)
#
sub verify
{
    my( $self, $cgi ) = @_;

    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    #if ( $session->is_expired ) { return 0; }
    my $user_login = $session->param("user_login");
    # If there is no user_login parameter, session is invalid
    if (!$user_login)  { return undef; }
    # CGI::Session doesn't quite work as advertised
    $cgi->param(-name=>'user_login',-value=>$user_login);
    return $user_login;
} #____________________________________________________________________________


###############################################################################
sub end
{
    my( $self, $cgi ) = @_;
  
    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    #$session->clear(["user_login"]);
    #$session->clear(["last_page"]);
    $session->delete();
} #____________________________________________________________________________


######
1;
