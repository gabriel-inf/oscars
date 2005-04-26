package AAAS::Client::Auth;
#
# package for user authentication
# Last modified: April 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)


use CGI;
use CGI::Session;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( verify_login_status encode_passwd generate_random_string );


### password encryption & random string generation (activation key, password reset (future), ...)
# sub encode_passwd
# sub generate_random_string


$psalt = 'oscars';

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



##### method verify_login_status
# In: ref to form data, and whether initial log in
#     Must have cookie containing valid session id
#     to be granted access.
# Out: 1 (logged in)/0 (not logged in)
sub verify_login_status
{
  my ($self, $form_data, $init_login) = @_;
  my ($cgi, $session, $sid, $cookie, $stored_dn);

  $cgi = CGI->new();
  if (defined($init_login))    # just logged in
  {
    $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    $sid = $session->id();
    $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param("dn", $form_data->{'dn'});
    print $cgi->header( -cookie=>$cookie );
    return( 1 );
  }
  else
  {
    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
        # Unauthorized user may know to set CGISESSID cookie. However,
        # an entirely new session (without the dn param) will be 
        # created if there is no valid session with that id.
    $stored_dn = $session->param("dn");
    if (!defined($stored_dn)) {
        print STDERR "no such parameter stored\n";
        return( 0 );
    }
    else
    {
       $form_data->{'dn'} = $stored_dn;
       print $cgi->header( );
       return( 1 ); }
    }
}



##### method encode_passwd
# In: $raw_password (plain text)
# Out: $crypted_password
#####
sub encode_passwd
{

  my ($raw_pwd) = $_;
  my( $crypted_pwd );
 
  $crypted_pwd = crypt( $raw_pwd, $psalt );
  return $crypted_pwd;
}


##### method generate_random_string
# In: $string_length
# Out: $random_string
# This sub routine takes care of generating a random string for all functions
#####
sub generate_random_string
{

  my $string_length = $_[0] + 0;	# make it a numeric value

  my @alphanumeric = ('a'..'z', 'A'..'Z', 0..9);
  my $random_string = join( '', map( $alphanumeric[ rand @alphanumeric ], 0 .. $string_Length ) );

  return $random_string;
}


##### End of Library File
# Don't touch the line below
1;
