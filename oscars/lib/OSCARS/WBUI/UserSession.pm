###############################################################################
package Client::UserSession;

# Handles user session.
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use CGI qw{:cgi};
use CGI::Session;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    # TODO:  Fix when have ROAM
    $self->{user_levels} = {
        'user' => 2,
        'engr' => 4,
        'admin' => 8,
    };
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
    $session->param('user_level', $results->{user_level});
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
# authorize_session:  Checks to see that a cookie containing a valid session
# id is set, and get authorization level.  TODO: will be completely redone.
#
# In:  ref to CGI instance
# Out: 1 (authorized)/0 (not authorized)
#
sub authorize_session
{
    my( $self, $cgi ) = @_;

    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});

    # Unauthorized user may know to set CGISESSID cookie. However,
    # an entirely new session (without the dn param) will be 
    # created if there is no valid session with that id.
    my $user_level = $session->param("user_level");
    if (!$user_level)  {
        return( undef );
    }
    else {
       $cgi->param(-name=>'user_level',-value=>$user_level);
       return( $user_level );
    }
} #____________________________________________________________________________


###############################################################################
sub end_session
{
    my( $self, $cgi ) = @_;
  
    my $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    $session->clear(["user_dn"]);
    $session->clear(["user_level"]);
} #____________________________________________________________________________


##############################################################################
# authorized:  Given the user level code, see if the user has the required
#              privilege
# 
sub authorized {
    my( $self, $user_level, $required_priv ) = @_;
 
    return($self->{user_levels}->{$required_priv} & $user_level );
} #____________________________________________________________________________


###############################################################################
#
sub get_str_level {
    my( $self, $level_flag ) = @_;

    my $level = "";
    $level_flag += 0;
    if ($self->{user_levels}->{admin} & $level_flag) {
        $level .= 'admin ';
    }
    if ($self->{user_levels}->{engr} & $level_flag) {
        $level .= 'engr ';
    }
    if ($self->{user_levels}->{user} & $level_flag) {
        $level .= 'user';
    }
    return( $level );
} #____________________________________________________________________________


######
1;
