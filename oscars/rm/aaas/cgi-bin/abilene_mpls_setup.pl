#
# abilene_mpls_setup.pl
#
# library for Junoscript MPLS tunnel setup on Abilene network
# Last modified: March 08, 2005
# Soo-yeon Hwang (dapi@umich.edu)

### Note on 3/8/2005: Right now, this script assumes that the back-end server can directly reach the routers via one ssh session.
# Otherwise, it won't work.

use JUNOS::Device;
use XML::DOM;

### Abilene server variable settings are in "lib_parseroute.cgi" ###

# router connection information
# when 'common' type is used for host type (the first hash key), 
# subkey 'hostname' should be provided when actual connection is made
%device_connection_info = (
	'common' => {
		access => "ssh",
		login => "junoscript",
		password => "PASSWORD_SCREENED"
	}
);

# XML template file location
%xml_template_file = (
	'build' => './abilene_mpls_conf_build.xml',
	'remove' => './abilene_mpls_conf_remove.xml'
);


##### sub Abilene_MPLS_Tunnel_Setup
# In: $OpMode [build/remove], $Reservation_ID, $Ingress_Router_Name, $Egress_Router_Name, $Ingress_Router_IPaddr, $Egress_Router_IPaddr, $MPLS_LSP_Bandwidth
# Out: $Result [1 (success)/$Error_Messages (fail)]
sub Abilene_MPLS_Tunnel_Setup
{

	# set AUTOFLUSH to true
	$| = 1;

	# define the constants used for the states in sub graceful_shutdown
	use constant {
		REPORT_SUCCESS => 1,
		REPORT_FAILURE => 0,
		STATE_CONNECTED => 1,
		STATE_LOCKED => 2,
		STATE_CONFIG_LOADED => 3,
	};

	# Initialize the XML Parser
	my $XML_Parser = new XML::DOM::Parser;

	# Start the sub routine operation
	my $OpMode = $_[0];
	my %OpConfig;
	@OpConfig{ 'reservation_id', 'ingress_name', 'egress_name', 'mpls_lsp_origin', 'mpls_lsp_destination', 'mpls_lsp_bandwidth' } = $_[1 .. 6];

	# xml file that contains the configuration to merge
	$OpConfig{'xml_template_file'} = $xml_template_file{$OpMode};

	# error message collection
	my $Error_Messages;

	# build a bi-directional MPLS tunnel from the origin to the destination
	# i.e. build one tunnel from ingress->egress, and another from egress->ingress
	foreach $Source_Endpoint ( @OpConfig{ 'mpls_lsp_origin', 'mpls_lsp_destination' } )
	{
		# Connect to the Junoscript server
		my $Jnx = new JUNOS::Device( %{ $device_connection_info{'common'} }, 'hostname' => $Source_Endpoint );
		$Jnx->connect() || return "Failed to connect to $Source_Endpoint.";
		unless ( ref $Jnx )
		{
			return  "Failed to connect to $Source_Endpoint.";
		}

		#
		# Lock the configuration database before making any changes
		# 
		my $Response = $Jnx->lock_configuration();
		my $Error = $Response->getFirstError();
		if ( $Error )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_CONNECTED, REPORT_FAILURE );
			return "Failed to lock configuration on $Source_Endpoint.  Reason: $Error->{message}.";
		}

		#
		# Load the configuration
		# 
		if ( ! -f $OpConfig{'xml_template_file'} )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_LOCKED, REPORT_FAILURE );
			return "Cannot load configuration in $OpConfig{'xml_template_file'}.";
		} 

		my $XML_String = &read_xml_file( $OpConfig{'xml_template_file'} );

		# Replace configure strings in the xml file with corresponding information
		if ( $Source_Endpoint eq $OpConfig{'mpls_lsp_origin'} )
		{
			my $LSP_Name = 'BRUW_' . $OpConfig{'reservation_id'} . '_' . $OpConfig{'ingress_name'} . '->' . $OpConfig{'egress_name'};

			if ( $OpMode eq "build" )
			{
				foreach ( $XML_String )
				{
					s/MPLS_LSP_NAME/$LSP_Name/;
					s/MPLS_LSP_FROM_ADDR/$OpConfig{'mpls_lsp_origin'}/;
					s/MPLS_LSP_TO_ADDR/$OpConfig{'mpls_lsp_destination'}/;
					s/MPLS_LSP_INSTALL_DESTINATION_PREFIX/$OpConfig{'mpls_lsp_destination'}\/32/;

					if ( $OpConfig{'mpls_lsp_bandwidth'} ne '' )
					{
						s/MPLS_LSP_BANDWIDTH/$OpConfig{'mpls_lsp_bandwidth'}/;
					}
					else
					{
						s/<bandwidth>.*?<\/bandwidth>//s;
					}

					# deprecated
#					if ( $_ =~ /<primary>/ )
#					{
#						s/<primary>MPLS_LSP_PRIMARY_PATH<\/primary>/<primary>$MPLS_LSP_Origin_Primary_Path<\/primary>/;
#					}
				}
			}
			else
			{
				$XML_String =~ s/<name>MPLS_LSP_NAME<\/name>/<name>$LSP_Name<\/name>/;
			}
		}
		elsif ( $Source_Endpoint eq $OpConfig{'mpls_lsp_destination'} )
		{
			my $LSP_Name = 'BRUW_' . $OpConfig{'reservation_id'} . '_' . $OpConfig{'egress_name'} . '->' . $OpConfig{'ingress_name'};

			if ( $OpMode eq "build" )
			{
				foreach ( $XML_String )
				{
					s/MPLS_LSP_NAME/$LSP_Name/;
					s/MPLS_LSP_FROM_ADDR/$OpConfig{'mpls_lsp_origin'}/;
					s/MPLS_LSP_TO_ADDR/$OpConfig{'mpls_lsp_destination'}/;
					s/MPLS_LSP_INSTALL_DESTINATION_PREFIX/$OpConfig{'mpls_lsp_destination'}\/32/;

					if ( $OpConfig{'mpls_lsp_bandwidth'} ne '' )
					{
						s/MPLS_LSP_BANDWIDTH/$OpConfig{'mpls_lsp_bandwidth'}/;
					}
					else
					{
						s/<bandwidth>.*?<\/bandwidth>//s;
					}

					# deprecated
#					if ( $_ =~ /<primary>/ )
#					{
#						s/<primary>MPLS_LSP_PRIMARY_PATH<\/primary>/<primary>$MPLS_LSP_Destination_Primary_Path<\/primary>/;
#					}
				}
			}
			else
			{
				$XML_String =~ s/MPLS_LSP_NAME/$LSP_Name/;
			}
		}

		my $Conf_Doc = $XML_Parser->parsestring( $XML_String ) if $XML_String;

		unless ( ref $Conf_Doc )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_LOCKED, REPORT_FAILURE );
			return "Cannot parse $OpConfig{'xml_template_file'}; Check to make sure the XML data is well-formed.";
		}

		#
		# Put the load_configuration in an eval block to make sure if the rpc-reply
		# has any parsing errors, the grace_shutdown will still take place.  Do
		# not leave the database in an exclusive lock state.
		#
		eval {
			$Response = $Jnx->load_configuration(
				format => "xml", 
				action => "merge",
				configuration => $Conf_Doc
			);
		};
		if ( $@ )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_CONFIG_LOADED, REPORT_FAILURE );
			return "Failed to load the configuration from $OpConfig{'xml_template_file'}. Reason: $@";
		} 

		unless ( ref $Response )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_LOCKED, REPORT_FAILURE );
			return "Failed to load the configuration from $OpConfig{'xml_template_file'}";
		}

		$Error = $Response->getFirstError();
		if ( $Error )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_CONFIG_LOADED, REPORT_FAILURE );
			print "Failed to load the configuration.  Reason: $Error->{message}";
		}

		#
		# Commit the change
		#

		# commit configuration from $OpConfig{'xml_template_file'}
		$Response = $Jnx->commit_configuration();
		$Error = $Response->getFirstError();
		if ( $Error )
		{
			&graceful_shutdown( $Jnx, $Request, STATE_CONFIG_LOADED, REPORT_FAILURE );
			return "Failed to commit configuration.  Reason: $Error->{message}.";
		}

		#
		# Cleanup
		#
		&graceful_shutdown( $Jnx, $Request, STATE_LOCKED, REPORT_SUCCESS );

	}

#	print "\nAll operations have been completed successfully.\n";
	return 1;

}
##### End of sub Abilene_MPLS_Tunnel_Setup


##### sub read_xml_file
# In: $Input_XML_Filename
# Out: $Input_XML_Filecontents
# Read XML from a file, stripping the <?xml version=...?> tag if necessary
sub read_xml_file
{

	my $input_file = shift;
    my $input_string = "";

    open(FH, $input_file) || return;

    while(<FH>)
	{
		next if /<\?xml.*\?>/;
		$input_string .= $_;
    }

    close(FH);

    return $input_string;

}
##### End of sub read_xml_file


##### sub graceful_shutdown
# 
# To gracefully shutdown.
# Recognizes 3 states:  1 connected, 2 locked, 3 config_loaded
# Put eval around each step to make sure the next step is performed no
# matter what.
#
sub graceful_shutdown
{

	my ($jnx, $req, $state, $success) = @_;

	if ($state >= STATE_CONFIG_LOADED)
	{
#		print "Rolling back configuration ...\n";
		eval {
			$jnx->load_configuration(rollback => 0);
		};
	}

	if ($state >= STATE_LOCKED)
	{
#		print "Unlocking configuration database ...\n";
		eval {
			$jnx->unlock_configuration();
		};
	}

	if ($state >= STATE_CONNECTED)
	{
#		print "Disconnecting from the router ...\n";
		eval {
			$jnx->request_end_session();
			$jnx->disconnect();
		};
	}

	if ( $success )
	{
#		die "REQUEST $req SUCCEEDED\n";
#		print "REQUEST $req SUCCEEDED\n";
	}
	else
	{
#		die "REQUEST $req FAILED\n";
	}

}
##### End of sub graceful_shutdown


##### End of Library File
# Don't touch the line below
1;
