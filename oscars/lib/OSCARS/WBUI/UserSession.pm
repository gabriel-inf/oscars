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

May 5, 2006

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
#   request has same session id, and has a login parameter set.
#   Note that this does not handle checking whether the user is in 
#   the database; that is handled by a method in the AAA prior to calling
#   this method.
#
# In:   ref to CGI instance, results of database user/password check
# Out:  None
#
sub start
{
    my( $self, $cgi, $response ) = @_;

    my $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    my $sid = $session->id();
    $session->param('login', $response->{login});
    $session->param('lastPage', $response->{GetInfo});
    $session->param('authorized', $response->{authorized});
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
    my $login = $session->param("login");
    my $authorized = $session->param("authorized");
    if ( $login && !$authorized ) { $authorized = { 'default' => 'default' }; }

    # If there is no login parameter, session is invalid
    if (!$login)  { return undef; }
    # CGI::Session doesn't quite work as advertised
    $cgi->param(-name=>'login',-value=>$login);
    if ( !$authorized->{default} ) {
        $cgi->param(-name=>'authorized',-value=>$authorized);
    }
    return( $login, $authorized);
} #____________________________________________________________________________


###############################################################################
sub end
{
    my( $self, $cgi ) = @_;
  
    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    #$session->clear(["login"]);
    #$session->clear(["lastPage"]);
    $session->delete();
} #____________________________________________________________________________


######
1;
