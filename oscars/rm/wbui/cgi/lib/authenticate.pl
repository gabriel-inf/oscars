#

# authenticate.pl
#
# library for user authentication
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

##### Settings Begin (Global variables) #####

# admin login name (id)
$admin_loginname_string = 'admin';

# admin user level
$admin_user_level = 10;

# non-activated user level (can't login until authorized & activated)
$non_activated_user_level = 0;

# read-only user levels (can make no changes to the database)
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

# admin login cookie name (anything unique is fine)
# don't change this value often because it would spoil all previously baked cookies
$admin_login_cookie_name = 'i2mplsadmlogin';

# user login cookie name (anything unique is fine)
# don't change this value often because it would spoil all previously baked cookies
$user_login_cookie_name = 'i2mplsusrlogin';

# admin tool gateway URI (admin login/registration screen)
$admin_tool_gateway_URI = 'https://discvenue.internet2.edu/athena/admin/form_admin_gateway.cgi';

# main service login URI (user login screen)
$main_service_login_URI = 'https://discvenue.internet2.edu/athena/form_login.cgi';

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
$account_activation_form_URI = 'https://discvenue.internet2.edu/athena/form_activateaccount.cgi';

##### Settings End #####


##### List of sub routines #####
# sub Verify_Login_Status
# sub Generate_Randomkey
# sub Read_Login_Cookie
# sub Set_Login_Cookie

##### List of sub routines End #####


##### sub Verify_Login_Status
# In: $Cookie_Name
# Out: $Login_Status_Check_Result [1 (logged in)/0 (not logged in)]
sub Verify_Login_Status
{

	my $Cookie_Name = $_[0];

	### retrieve cookie
	my %Data_From_Cookie;
	@Data_From_Cookie{ 'cookiekey_id', 'user_loginname', 'randomkey' } = &Read_Login_Cookie( $Cookie_Name );

	my $Login_Status_Check_Result = 0;

	# if there exists a login cookie
	if ( $Data_From_Cookie{'cookiekey_id'} ne '' )
	{
		### TODO:  Check whether admin account is set up in the database
	}

	return $Login_Status_Check_Result;

}
##### sub Verify_Login_Status


##### sub Generate_Randomkey
# In: $User_loginname
# Out: $Randomkey (md5 hex digest of user_loginname plus unix timestamp)
sub Generate_Randomkey
{

	# for random key generation
	use Digest::MD5 qw( md5_hex );

	my $String = $_[0] . time;
	my $Digest = md5_hex( $String );

	return $Digest;

}
##### End of sub Generate_Randomkey


##### sub Read_Login_Cookie
# In: $Cookie_Name
# Out: $Cookiekey_ID, $User_Loginname, $Random_Key
sub Read_Login_Cookie
{

	my $Cookie_Name = $_[0];
	my %Cookie_Data;

	my @Key_Value_Pairs = split( /;\s/, $ENV{'HTTP_COOKIE'} );

	foreach $_ ( @Key_Value_Pairs )
	{
		my( $Key, $Value ) = split( /=/, $_ );
		$Value =~ tr/+/ /;
		$Value =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack( "C", hex( $1 ) )/eg;
		$Cookie_Data{$Key} = $Value;
	}

	my @Login_Info = split( /:/, $Cookie_Data{$Cookie_Name} );

	return @Login_Info;

}
##### End of sub Read_Login_Cookie


##### sub Set_Login_Cookie
# In: $Type [login/logout], $Cookie_Name, $Cookiekey_ID, $User_Loginname, $Random_Key
# Out: $Set-Cookie_String
sub Set_Login_Cookie
{

	my $Cookie_Type = $_[0];
	my $Cookie_Name = $_[1];
	my $Cookie_String = join( ':', @_[2 .. 4] );

	# URL style %XX encoding is recommended according to the original Netscape cookie spec
	$Cookie_String =~ s/(\W)/sprintf( "%%%x", ord( $1 ) )/eg;

	if ( $Cookie_Type eq 'logout' )
	{
		my $Cookie_Expiration_Date = &Create_Time_String( 'cookie', time - 3600 );
		return "Set-Cookie: $Cookie_Name=$Cookie_String; expires=$Cookie_Expiration_Date\n";
	}
	elsif ( $Cookie_Type eq 'login' )
	{
		return "Set-Cookie: $Cookie_Name=$Cookie_String\n";
	}

}
##### End of sub Set_Login_Cookie


##### End of Library File
# Don't touch the line below
1;
