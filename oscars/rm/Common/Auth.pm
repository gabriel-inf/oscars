package Common::Auth;
#
# package for user authentication
# Last modified: August 5, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)


use CGI;
use CGI::Session;

### class Auth
# start_session:          Sets cookies associated with session id
# verify_session:         Checks for cookie containing valid session id,
#                         authorization level
# end_session:            Unsets cookies
# generate_random_string: Password encryption & random string generation (for
#                         activation key, password reset (future), ...)


##############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;
    $self->{starting_page} = 'https://oscars.es.net';
}
######


##############################################################################
# start_session: Sets cookie containing session id to be used in granting
#   access.  Note that this does not handle checking whether the user is in 
#   the database; that is handled by a method in the AAAS SOAPClient package.
#
# In:   ref to CGI instance
# Out:  None
#
sub start_session
{
    my ($self, $cgi, $login_results) = @_;
    my ($session, $sid, $cookie);

    $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    $sid = $session->id();
    $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param("user_dn", $cgi->param('user_dn'));
    $session->param("user_level", $login_results->{'user_level'});
    $session->param("timezone_offset", $cgi->param('timezone_offset'));
    return( $cgi->param('user_dn'), $login_results->{'user_level'}, $sid );
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
    my ($self, $cgi) = @_;
    my ($session, $stored_dn, $user_level, $timezone_offset);

    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});

    # Unauthorized user may know to set CGISESSID cookie. However,
    # an entirely new session (without the dn param) will be 
    # created if there is no valid session with that id.
    $stored_dn = $session->param("user_dn");
    $user_level = $session->param("user_level");
    $timezone_offset = $session->param("timezone_offset");
    if (!$stored_dn)  {
        return( undef, undef, undef, $self->{starting_page} );
    }
    else {
       $cgi->param(-name=>'user_dn',-value=>$stored_dn);
       $cgi->param(-name=>'user_level',-value=>$user_level);
       $cgi->param(-name=>'timezone_offset',-value=>$timezone_offset);
       return( $stored_dn, $user_level, $timezone_offset, $self->{starting_page} );
    }
}
######


##############################################################################
sub end_session
{
    my( $self, $cgi ) = @_;
    my ($session, $stored_dn);
  
    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    $session->clear(["user_dn"]);
    $session->clear(["user_level"]);
    $session->clear(["timezone_offset"]);
}
######

##############################################################################
# generate_random_string: Takes care of generating a random string for all
#     functions
#
# In: $string_length
# Out: $random_string
#
sub generate_random_string
{
    my $self = @_;
    my $string_length = $_[0] + 0;	# make it a numeric value

    my @alphanumeric = ('a'..'z', 'A'..'Z', 0..9);
    my $random_string = join( '', map( $alphanumeric[ rand @alphanumeric ], 0 .. $string_Length ) );

    return $random_string;
}
######

##############################################################################
# authorized:  Given the user level string, see if the user has the required
#              privilege 
#
sub authorized {
    my( $self, $user_level, $required_priv ) = @_;
 
    for my $priv (split(' ', $user_level)) {
        if ($priv eq $required_priv) {
            return( 1 );
        }
    }
    return( 0 );
}
######

######
1;
