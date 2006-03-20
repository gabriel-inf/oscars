###############################################################################
package OSCARS::PSS::JnxLSP;

# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:  Class and methods to setup/teardown LSPs on Juniper routers.
# Last Modified:  March 6, 2006


use strict;

use JUNOS::Device;
use JUNOS::Trace;
use XML::DOM;

use Data::Dumper;

require Exporter;
our @ISA = qw(Exporter);
our @EXPORT = qw();

# JUNOScript constants

use constant _STATE_CONNECTED => 1;
use constant _STATE_LOCKED => 2;
use constant _STATE_CONFIG_LOADED => 3;

# Global variables

my ($_configFormat) = 'xml';  # Default Junoscript configuration format is XML.
my ($_loadAction) = 'merge';  # Default load action is 'merge'.


sub new {
    my ($class, $args) = @_;

    my ($self) = {%$args};
    bless($self, $class);
    $self->initialize();
    return($self);
}

sub initialize {
    my ($self) = @_;

    # Clear error message.
    $self->{errMsg} = 0;
  
        # not currently on a per-reservation basis
    $self->{'lsp_setup-priority'} =
                                 $self->{configs}->{pss_conf_setup_priority};
    $self->{'lsp_reservation-priority'} =
                                 $self->{configs}->{pss_conf_resv_priority};
    $self->{external_interface_filter} =
                                 $self->{configs}->{pss_conf_ext_if_filter};

    $self->{firewall_filter_marker} =
                                 $self->{configs}->{pss_conf_firewall_marker};
    return();
} #____________________________________________________________________________


##############################################################################
# configure_lsp:  Configure an LSP on a Juniper router.
# In:  lspOp => 1=>setup, 0=>teardown
# Out: <none>
#
sub configure_lsp {
    my ($self, $lsp_op, $logger) = @_;

    my $xmlFile;

    $self->{logger} = $logger;
    # For LSP setup, use setupXmlFile
    # for teardown, use teardownXmlFile.
    # TODO:  FIX hard-wired directory
    if ($lsp_op == 1)  {
        $xmlFile = '/home/oscars/PSS/xml/' .
                    $self->{configs}->{pss_conf_setup_file};
    }
    else  {
        $xmlFile = '/home/oscars/PSS/xml/' .
                    $self->{configs}->{pss_conf_teardown_file};
    }

    my $xmlString = $self->read_xml_file($xmlFile);

    # Execute the Junoscript configuration changes if there is no error.
    if (!($self->get_error()))  {
        $self->execute_configuration_change($xmlString);
    }
    if ($self->{logger}) { $self->update_log( $xmlString ); }
    return;
} #____________________________________________________________________________


##############################################################################
# get_lsp_status: Get the LSP status on a Juniper router.
# In:  router => target router
#      lspName => name of the LSP to query.
# Out: -1 => NA (e.g not found), 0 => down, 1 => up
#
sub get_lsp_status {
    my ($self) = @_;

    my (@resultArray, $_result);
    my $state = -1;

    $_result = $self->execute_operational_command("get_mpls_lsp_information");
    @resultArray = split(/\n/, $_result);

    while (defined($resultArray[0]))  {
        $_result = shift(@resultArray);

        # Update the value with the LSP state (0 => down, 1 => up).
        # NB: In the XML query, we'll see the <state> BEFORE the <name>
        if  ($_result =~ m/^<lsp-state>(\w+)<\/lsp-state>$/)  {
            if (lc($1) eq 'up')  {
                $state = 1;
            }
            elsif (lc($1) eq 'dn')  {
                $state = 0;
            }
            else  {
                $state = -1;
            }
        }

        # If we locate the LSP, set the status to 1.
        elsif ($_result =~ m/^<name>$self->{name}<\/name>$/)  {
            return($state);
        }
    }
    return(-1);
} #____________________________________________________________________________


##############################################################################
# get_error:  Return the error message (0 if none).
# In:  <none>
# Out: Error message
#
sub get_error {
    my ($self) = @_;

    return($self->{errMsg});
} #____________________________________________________________________________


#################
# Private methods
#################

##############################################################################
# read_xml_file:
#   Read XML from a file, stripping the <?xml version=...?> tag if necessary,
#   and replacing variables in the XML file if required.
#
# In:  xmlFile => XML filename
# Out: xmlOutput => XML string to push to routers, 0 => failure
#
sub read_xml_file {
    my ($self, $xmlFile) = @_;

    my $config_name;
    my ($xmlInput, $xmlOutput);

    # Clear error message.
    $self->{errMsg} = 0;

    $xmlOutput = '';

    if (!(open(_XMLFILE, $xmlFile)))  {
        $self->{errMsg} = "ERROR: Can't open XML file $xmlFile\n";
        return(0);
    }
    while ($xmlInput = <_XMLFILE>)  {

        # Ignore <?xml version=...?>
        next if ($xmlFile =~ m/<\?xml.*\?>/);

        for $config_name (keys %{$self})  {
            if ($config_name ne 'configs') {
                $xmlInput =~ s/<user-var>$config_name<\/user-var>/$self->{$config_name}/g;
            }
        }

        # If the string has a user-var that is not defined, throw the line away.
        # (Most likely it was an optional Junoscript configuration line.)
        next if ($xmlInput =~ m/<user-var>[a-zA-Z0-9_-]*<\/user-var>/);

        $xmlOutput .= $xmlInput;
    }
    close(_XMLFILE);
    return($xmlOutput);
} #____________________________________________________________________________


##############################################################################
# graceful_shutdown:  Gracefully shutdown.
#   Recognizes 3 states: 1 connected, 2 locked, 3 config_loaded
#   Puts eval around each step to make sure the next step is always performed.
#
# In:  jnx => JUNOS::Device   state => connected, locked, config_loaded
# Out: <none>
#
sub graceful_shutdown {
    my ($jnx, $state) = @_;

    if ($state >= _STATE_CONFIG_LOADED) {
        eval {
            $jnx->load_configuration(rollback => 0);
        };
    }

    if ($state >= _STATE_LOCKED)  {
        eval {
            $jnx->unlock_configuration();
        };
    }

    if ($state >= _STATE_CONNECTED)  {
        eval {
            $jnx->request_end_session();
            $jnx->disconnect();
        }
    }
} #____________________________________________________________________________


##############################################################################
# execute_configuration_change: Make configuration changes in a Juniper router.
#
# In: xmlString => configuration in XML format
# Out: <none>
#
sub execute_configuration_change {
    my ($self, $xmlString) = @_;

    my (%jnxInfo) = (
        'access' => $self->{configs}->{pss_conf_access},
        'login'  => $self->{configs}->{pss_conf_login},
        'password' => $self->{configs}->{pss_conf_passwd},
        'hostname' => $self->{lsp_from}
    );
    my $jnx;

    # Clear error message.
    $self->{errMsg} = 0;

    # Initialize the XML Parser.
    my ($_xmlParser) = new XML::DOM::Parser;

    if (!$self->{configs}->{pss_conf_allow_lsp}) {
        return("Not configured to allow JUNOScript commands");
    }

    # Connect to the JUNOScript server.
    eval {
        ($jnx) = new JUNOS::Device(%jnxInfo);
        unless (ref $jnx) {
            $self->{errMsg} = "ERROR: $jnxInfo{hostname}: failed to connect.\n";
            return();
        }
    };
    if ($@) {
	if ($self->{logger}) {
            $self->{logger}->add_string("ignoring exception $@\n");
	}
        return;
    }

    # Lock the Junoscript configuration database before making any changes
    my $jnxRes = $jnx->lock_configuration();
    my $error = $jnxRes->getFirstError();
    if ($error)  {
        $self->{errMsg} = "ERROR: $jnxInfo{hostname}: failed to lock configuration.  Reason: $error->{message}.\n";
        graceful_shutdown($jnx, _STATE_CONNECTED);
        return();
    }

    # Load the Junoscript configuration.
    my $xmlDoc = $_xmlParser->parsestring($xmlString);
    unless (ref($xmlDoc)) {
        $self->{errMsg} = "ERROR: Cannot parse $xmlString, check to make sure the XML data is well-formed\n";
        graceful_shutdown($jnx, _STATE_LOCKED);
        return();
    }

    # Put the load_configuration in an eval block to make sure if the rpc-reply
    # has any parsing errors, the grace_shutdown will still take place.  Do
    # not leave the database in an exclusive lock state.
    eval {
        $jnxRes = $jnx->load_configuration(
                     'format' => $_configFormat,
                     'action' => $_loadAction,
                     'configuration' => $xmlDoc);
    };
    if ($@) {
        $self->{errMsg} = "ERROR: Failed to load the configuration.   Reason: $@\n";
        graceful_shutdown($jnx, _STATE_CONFIG_LOADED);
        return();
    }

    unless (ref($jnxRes))  {
        $self->{errMsg} = "ERROR: Failed to load the configuration\n";
        graceful_shutdown($jnx, _STATE_LOCKED);
        return();
    }

    $error = $jnxRes->getFirstError();
    if ($error)  {
        $self->{errMsg} = "ERROR: Failed to load the configuration.  Reason: $error->{message}\n";
        graceful_shutdown($jnx, _STATE_CONFIG_LOADED);
        return();
    }

    # Commit the change.
    $jnxRes = $jnx->commit_configuration();
    $error = $jnxRes->getFirstError();
    if ($error)  {
        $self->{errMsg} = "ERROR: Failed to commit configuration.  Reason: $error->{message}.\n";
        graceful_shutdown($jnx, _STATE_CONFIG_LOADED);
    }
  
    # Configuration successfully commited.
    graceful_shutdown($jnx, _STATE_LOCKED);
    return();
} #___________________________________________________________________________


##############################################################################
# execute_operational_command: Perform operational command in a Juniper router.
#
# In:  router => router to execute command on
#      command => command to execute
# Out: 0 => failure
#      results of query
#
sub execute_operational_command {
    my ($self, $command) = @_;

    my %jnxInfo = (
        'access' => $self->{configs}->{pss_conf_access},
        'login'  => $self->{configs}->{pss_conf_login},
        'password' => $self->{configs}->{pss_conf_password},
        'hostname' => $self->{lsp_from}
    );
    my %queryArgs = ('detail' => 0);  # 1 => "extensive" or "detail" view.

    # Clear error message.
    $self->{errMsg} = 0;

    if (!$self->{configs}->{pss_conf_allow_lsp}) {
        return("Not configured to allow JUNOScript commands");
    }
    # Connect to the JUNOScript server.
    my ($jnx) = new JUNOS::Device(%jnxInfo);
    unless (ref $jnx) {
        $self->{errMsg} = "ERROR: $jnxInfo{hostname}: failed to connect.\n";
        return(0);
    }

    # Send the command and receive a XML::DOM object.
    my $jnxRes = $jnx->$command( %queryArgs );
    unless (ref($jnxRes))  {
        $self->{errMsg} = "ERROR: $jnxInfo{hostname}: failed to execute command $command.\n";
        return(0);
    }

    # Check and see if there were any errors in executing the command.
    my $error = $jnxRes->getFirstError();
    if ($error)  {
        # jrl $self->{errMsg} = "ERROR: $jnxInfo{hostname} - ", $error->{message}, "\n";
        $self->{errMsg} = "ERROR: $jnxInfo{hostname} - " . $error->{message} . "\n";
        return(0);
    }
    return($jnxRes->toString());
} #____________________________________________________________________________


##############################################################################
#
sub update_log {
    my( $self, $xmlString) = @_;

    my $errmsg = $self->get_error();
    if ($errmsg)  { $xmlString .= "\n\n$errmsg\n"; }
    $self->{logger}->add_string($xmlString);
} #____________________________________________________________________________


######
1;
