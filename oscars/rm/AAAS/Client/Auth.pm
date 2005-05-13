package AAAS::Client::Auth;
#
# package for user authentication
# Last modified: April 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)


use CGI;
use CGI::Session;

### password encryption & random string generation (activation key, password reset (future), ...)
# sub generate_random_string


# admin login name (id)
$admin_dn = 'admin';

# admin user level
$admin_user_level = 10;

# non-activated user level (can't login until authorized & activated)
$non_activated_user_level = 0;

# read-only user level (can make no changes to the database)
@read_only_user_levels = ( '2' );

# user level map & description
# the descriptions will be used for the user list page
# level 0 needs admin authorization to use the system (level upgrade)
# level 10 is reserved for system admin
%user_level_description = (
	0 => 'Authorization Required',
	1 => 'Normal User with All Privileges',
	2 => 'Normal User with Read Privilege Only',
	10 => 'Administrator',
	'pending' => 'Pending User Activation',
);

# user account activation key size (in characters)
$account_activation_key_size = '35';

# admin tool gateway URI (admin login/registration screen)
$admin_tool_gateway_URI = 'https://oscars.es.net/admin/';

# main service login URI (user login screen)
$main_service_login_URI = 'https://oscars.es.net/';

# hardcoded path to the sendmail binary and its flags
# sendmail is not used in this library, but in multiple other places related to user registration
# thus the setting is here
#
# sendmail options:
#    -n  no aliasing
#    -t  read message for "To:"
#    -oi don't terminate message on line containing '.' alone
$sendmail_binary_path_and_flags = '/usr/sbin/sendmail -t -n -oi';

# user account activation form URI
$account_activation_form_URI = 'https://oscars.es.net/user/activateaccount.phtml';


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
    my ($self, $cgi) = @_;
    my ($session, $sid, $cookie);

    $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    $sid = $session->id();
    $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param("dn", $cgi->param('dn'));
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
    my ($session, $stored_dn);

    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});

    # Unauthorized user may know to set CGISESSID cookie. However,
    # an entirely new session (without the dn param) will be 
    # created if there is no valid session with that id.
    $stored_dn = $session->param("dn");
    if (!$stored_dn)  {
        return( $stored_dn );
    }
    else {
       $cgi->param(-name=>'dn',-value=>$stored_dn);
       print $cgi->header( );
       return( $stored_dn );
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
