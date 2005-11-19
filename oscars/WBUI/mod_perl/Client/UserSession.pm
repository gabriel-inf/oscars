package Client::UserSession;

# Handles user session.
# Last modified:  November 17, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Error qw(:try);
use Data::Dumper;

use CGI;
use CGI::Session;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
}
######


##############################################################################
# start_session: Sets cookie containing session id to be used in granting
#   access.  Note that this does not handle checking whether the user is in 
#   the database; that is handled by a method in the AAAS.
#
# In:   ref to CGI instance
# Out:  None
#
sub start_session
{
    my( $self, $cgi ) = @_;

    my $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    my $sid = $session->id();
    my $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param("auth_token", $cgi->param('auth_token'));
    return( $cgi->param('auth_token'), $sid );
}
######

##############################################################################
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
    my $stored_token = $session->param("auth_token");
    if (!$stored_token)  {
        return( undef );
    }
    else {
       $cgi->param(-name=>'auth_token',-value=>$stored_token);
       return( $stored_token );
    }
}
######

##############################################################################
sub end_session
{
    my( $self, $cgi ) = @_;
  
    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    $session->clear(["auth_token"]);
}
######

######
1;
