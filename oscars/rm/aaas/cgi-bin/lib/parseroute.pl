#

# parseroute.pl
#
# library for parsing routes
# Last modified: Monday, June 14, 2004
# Soo-yeon Hwang (dapi@umich.edu)

# for host name lookup
use Socket;
use Net::hostent;

# for ssh connection
use Net::SSH::Perl;

##### Abilene server variable settings #####
# Abilene nms login server address
$abilene_nms_login_server_addr = 'nms-login.abilene.ucaid.edu';

# Abilene nodes addresses
%abilene_nms2_nodes_addr = (
	"atla" => "198.32.10.194",
	"chin" => "198.32.10.202",
	"dnvr" => "198.32.10.210",
	"hstn" => "198.32.10.218",
	"ipls" => "198.32.10.226",
	"kscy" => "198.32.10.234",
	"losa" => "198.32.10.242",
	"nycm" => "198.32.10.250",
	"snva" => "198.32.11.138",
	"sttl" => "198.32.11.130",
	"wash" => "198.32.11.146"
);

# an Abilene interface IP address begins with... (don't delete the dot at the end)
$abilene_interface_addr_prefix = '198.32.8.';

# Abilene node login user name
$abilene_login_username = 'bdr';

# hardcoded path to the application user's ssh private key
$ssh_identity_file_path = '/home/dapi/.ssh/id_rsa';

# hardcoded path to the traceroute binary (on Abilene nodes)
$traceroute_binary_path = '/usr/sbin/traceroute';

##### End of Abilene server variable settings #####


### sub Get_Abilene_Node_Name ###
# In: $Abilene_Hop_IP_Addr
# Out: $Abilene_Hop_DNS_Name
sub Get_Abilene_Node_Name
{

	my $Hop_IP_Addr = $_[0];
	my $Hop_Host_Name;
	my $h;

	unless ( $h = gethost( $Hop_IP_Addr ) )
	{
		# if the host lookup fails, it means that the $Hop_IP_Addr must be 198.32.8.69, ipls node's interface address
		return 'ipls';
	}

	$Hop_Host_Name = lc( $h->name );

	$Hop_Host_Name =~ s/ng-.+$//ig;

	if ( $Hop_Host_Name !~ /\w+/ )
	{
		$Hop_Host_Name = 'ipls';
	}

	return $Hop_Host_Name;

}
### End of sub Get_Abilene_Node_Name ###


### sub Abilene_Node_Lookup ###
# In: $Abilene_Node_to_Connect ['ipls' (if origin)/the closest node to origin (if destination)], $Host_IP_Addr
# Out: $Result [1(success)/'command_fail'(fail)/'non_abilene'(fail)], $Closest_Abilene_Node (only when $Result eq 1)
sub Abilene_Node_Lookup
{

	my( $Abilene_Node_to_Connect, $Host_IP_Addr ) = @_;

	# connect to the Abilene node login server via SSH
	# open ssh session to nms-login.abilene.ucaid.edu
	# uses unenrtyped private key to perform noninteractive login...
	my %ssh_login_params = (
		protocol => "2",
		identity_files => [ $ssh_identity_file_path ],
		debug => "1",
#		cipher => "blowfish-cbc",
		options => [ "BatchMode yes" ]
	);

	my $ssh = Net::SSH::Perl->new( $abilene_nms_login_server_addr, %ssh_login_params );
	$ssh->login( $abilene_login_username );

	my( $Closest_Abilene_Node, @Abilene_Hops );

	# traceroute uses -n option to speed things up (no dns lookup on each hop)
	my $Command = 'ssh -2 -l ' . $abilene_login_username . ' ' . $abilene_nms2_nodes_addr{$Abilene_Node_to_Connect} . ' "' . $traceroute_binary_path . ' -n ' . $Host_IP_Addr . '"';
	my( $ssh_out, $ssh_err, $ssh_exit ) = $ssh->cmd( $Command );

	# $ssh_exit: 1 if fail, 0 if succeed
	if ( $ssh_exit )
	{
		return 'command_fail';
	}

	chomp $ssh_out;

	my @Traceroute_Output = split( /\n/, $ssh_out );

	foreach $Tr_Line ( @Traceroute_Output )
	{
		if ( $Tr_Line =~ /\s+($abilene_interface_addr_prefix\d{1,3})\s+/ )
		{
			push( @Abilene_Hops, $1 );
		}
	}

	if ( $#Abilene_Hops >= 0 )
	{
		# found it!
		$Closest_Abilene_Node = &Get_Abilene_Node_Name( $Abilene_Hops[$#Abilene_Hops] );
		return 1, $Closest_Abilene_Node;
	}
	else
	{
		# this host is not connected to the Abilene network
		return 'non_abilene';
	}

}
### End of sub Abilene_Node_Lookup ###


##### End of Library File
# Don't touch the line below
1;
