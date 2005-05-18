package AAAS::Client::Auth;
#
# package for user authentication
# Last modified: May 17, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)


use CGI;
use CGI::Session;

### class Auth
# set_login_status:       Sets cookie containing session id
# verify_login_status:    Checks for cookie containing valid session id,
#                         authorization level
# logout:                 Unsets cookies
# generate_random_string: Password encryption & random string generation (for
#                         activation key, password reset (future), ...)


######################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

######################################################################
sub initialize {
    my ($self) = @_;
}


##### method set_login_status
#
# Sets cookie containing session id
# to be used in grantng access.
# In:   ref to CGI instance
# Out:  None
sub set_login_status
{
    my ($self, $cgi, $login_results) = @_;
    my ($session, $sid, $cookie);

    $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    $sid = $session->id();
    $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param("dn", $cgi->param('dn'));
    $session->param("admin_required", $cgi->param('admin_required'));
    $session->param("level", $login_results->{'user_level'});
    print $cgi->header( -cookie=>$cookie );
}


##### method verify_login_status
#
# Must have cookie containing valid session id
# to be granted access.
#
# In:  ref to CGI instance
# Out: 1 (logged in)/0 (not logged in)
sub verify_login_status
{
    my ($self, $cgi) = @_;
    my ($session, $stored_dn, $user_level, $admin_required);

    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});

    # Unauthorized user may know to set CGISESSID cookie. However,
    # an entirely new session (without the dn param) will be 
    # created if there is no valid session with that id.
    $stored_dn = $session->param("dn");
    $user_level = $session->param("level");
    $admin_required = $session->param("admin_required");
    if (!$stored_dn)  {
        return( undef, undef, undef );
    }
    else {
       $cgi->param(-name=>'dn',-value=>$stored_dn);
       $cgi->param(-name=>'admin_required',-value=>$admin_required);
       $cgi->param(-name=>'level',-value=>$user_level);
       print $cgi->header( );
       return( $stored_dn, $user_level, $admin_required );
    }
}



sub logout
{
    my( $self, $cgi ) = @_;
    my ($session, $stored_dn);
  
    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    $session->clear(["dn"]);
}



##### method generate_random_string
# In: $string_length
# Out: $random_string
# This sub routine takes care of generating a random string for all functions
#####
sub generate_random_string
{
    my $self = shift;
    my $string_length = $_[0] + 0;	# make it a numeric value

    my @alphanumeric = ('a'..'z', 'A'..'Z', 0..9);
    my $random_string = join( '', map( $alphanumeric[ rand @alphanumeric ], 0 .. $string_Length ) );

    return $random_string;
}


##### End of Library File
# Don't touch the line below
1;
