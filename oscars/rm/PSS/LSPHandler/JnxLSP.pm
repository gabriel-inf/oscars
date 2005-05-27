#####
#
# Package: JnxLSP.pm
# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:
#   Sub-routines to setup/teardown LSPs on Juniper routers.
#
#####

package PSS::LSPHandler::JnxLSP;

use strict;

use JUNOS::Device;
use JUNOS::Trace;
use XML::DOM;
use Config::Auto;

use Data::Dumper;

require Exporter;
our @ISA = qw(Exporter);
our @EXPORT = qw();


#####
#
# Constant definitions.
#
#####
# JUNOScript constants

use constant _STATE_CONNECTED => 1;
use constant _STATE_LOCKED => 2;
use constant _STATE_CONFIG_LOADED => 3;

#####
#
# Global variables.
#
#####
my ($_configFormat) = 'xml';  # Default Junoscript configuration format is XML.
my ($_loadAction) = 'merge';  # Default load action is 'merge'.


#####
#
# Public methods
#
#####

#####
#
# Method: new
# Description:
#   Create a jnxLSP.
# Input: <none>
# Output: new object
#
#####
sub new
{
    my ($_class, $_args) = @_;
    my ($_self) = {%$_args};

    # Bless $_self into designated class.
    bless($_self, $_class);

    # Initialize.
    $_self->initialize();

    return($_self);
}

#####
#
# Method: configure_lsp
# Description:
#   Configure an LSP on a Juniper router.
# Input: lspOp => 1=>setup, 0=>teardown
# Output: <none>
#
#####
sub configure_lsp
{
    my ($_self, $_lspOp, $_r) = @_;
    my ($_xmlFile);
    my ($_xmlInput);
    my ($_xmlString) = '';

    # For LSP setup, use setupXmlFile
    # for teardown, use teardownXmlFile.
    if ($_lspOp == 1)  {
        $_xmlFile = $ENV{'OSCARS_HOME'} . '/PSS/xml/' . $_self->{'jnxLSPConf'}->{'setupXmlFile'};
    }
    else  {
        $_xmlFile = $ENV{'OSCARS_HOME'} . '/PSS/xml/' . $_self->{'jnxLSPConf'}->{'teardownXmlFile'};
    }

    $_xmlString = $_self->read_xml_file($_xmlFile);
    $_self->update_log($_r, $_xmlString);

    # Execute the Junoscript configuration changes if there is no error.
    if (not($_self->get_error()))  {
        $_self->execute_configuration_change($_xmlString);
    }
    return;
}

#####
#
# Method: get_lsp_status
# Description:
#   Get the LSP status on a Juniper router.
# Input: router => target router
#        lspName => name of the LSP to query.
# Output: -1 => NA (e.g not found)
#         0 => down
#         1 => up
#
#####
sub get_lsp_status
{
    my ($_self) = @_;
    my (@_resultArray, $_result);
    my ($_found) = 0;
    my ($_state) = -1;

    $_result = $_self->execute_operational_command("get_mpls_lsp_information");
    @_resultArray = split(/\n/, $_result);

    while (defined($_resultArray[0]))  {
        $_result = shift(@_resultArray);

        # Update the value with the LSP state (0 => down, 1 => up).
        # NB: In the XML query, we'll see the <state> BEFORE the <name>
        if  ($_result =~ m/^<lsp-state>(\w+)<\/lsp-state>$/)  {
            if (lc($1) eq 'up')  {
                $_state = 1;
            }
            elsif (lc($1) eq 'dn')  {
                $_state = 0;
            }
            else  {
                $_state = -1;
            }
        }

        # If we locate the LSP, set the status to 1.
        elsif ($_result =~ m/^<name>$_self->{'name'}<\/name>$/)  {
            return($_state);
        }
    }
    return(-1);
}

#####
#
# Method: get_error
# Description:
#   Return the error message (0 if none).
# Input: <none>
# Output: Error message
#
#####
sub get_error
{
    my ($_self) = @_;

    return($_self->{'errMsg'});
}


#####
#
# Private methods
#
#####

#####
#
# Method: initialize
# Description:
#   Initialize jnxLSP with default values if not already populated.
# Input: <none>
# Output: <none>
#
#####
sub initialize 
{
    my ($_self) = @_;

    # read configuration file for this package
    $_self->{'jnxLSPConf'} = Config::Auto::parse($ENV{'OSCARS_HOME'} . '/oscars.cfg');

    # Clear error message.
    $_self->{'errMsg'} = 0;
  
    if (not(defined($_self->{'lsp_class-of-service'})))  {
        $_self->{'lsp_class-of-service'} = $_self->{'jnxLSPConf'}->{'CoS'};
    }
    if (not(defined($_self->{'lsp_setup-priority'})))  {
        $_self->{'lsp_setup-priority'} = $_self->{'jnxLSPConf'}->{'setupPriority'};
    }
    if (not(defined($_self->{'lsp_reservation-priority'})))  {
        $_self->{'lsp_reservation-priority'} = $_self->{'jnxLSPConf'}->{'resvPriority'};
    }
    if (not(defined($_self->{'policer_burst-size-limit'})))  {
        # Burst size ~1% of bandwidth. (Left undone for now.)
        $_self->{'policer_burst-size-limit'} = '1m';
    }
    if (not(defined($_self->{'external_interface_filter'})))  {
        $_self->{'external_interface_filter'} = $_self->{'jnxLSPConf'}->{'extIfFilter'};
    }
    if (not(defined($_self->{'firewall_filter_marker'})))  {
        $_self->{'firewall_filter_marker'} = $_self->{'jnxLSPConf'}->{'firewallFilterMaker'};
    }
    return();
}

#####
#
# Subroutine: read_xml_file
# Description:
#   Read XML from a file, stripping the <?xml version=...?>
#   tag if necessary, and replacing variables in the XML file
#   if required.
# Input: xmlFile => XML filename
# Output: xmlOutput => XML string to push to routers
#         0 => failure
#
#####
sub read_xml_file
{
    my ($_self, $_xmlFile) = @_;
    my ($_lspInfo);
    my ($_xmlInput, $_xmlOutput);

    # Clear error message.
    $_self->{'errMsg'} = 0;

    $_xmlOutput = '';

    $_self->{'errMsg'} = 0;
    if (not(open(_XMLFILE, $_xmlFile)))  {
        $_self->{'errMsg'} = "ERROR: Can't open XML file $_xmlFile\n";
        return(0);
    }

    while ($_xmlInput = <_XMLFILE>)  {

        # Ignore <?xml version=...?>
        next if ($_xmlFile =~ m/<\?xml.*\?>/);

        foreach $_lspInfo (keys %{$_self})  {
            $_xmlInput =~ s/<user-var>$_lspInfo<\/user-var>/$_self->{$_lspInfo}/g;
        }

        # If the string has a user-var that is not defined, throw the line away.
        # (Most likely it was an optional Junoscript configuration line.)
        next if ($_xmlInput =~ m/<user-var>[a-zA-Z0-9_-]*<\/user-var>/);

        $_xmlOutput .= $_xmlInput;
    }
    return($_xmlOutput);
}

#####
#
# Subroutine: graceful_shutdown
# Description:
#   To gracefully shutdown.  Recognized 3 states:
#   1 connected, 2 locked, 3 config_loaded
#   Put eval around each step to make sure the next
#   step is performed no matter what.
# Input: jnx => JUNOS::Device
#        state => connected, locked, config_loaded
# Output: <none>
#
#####
sub graceful_shutdown
{
    my ($_jnx, $_state) = @_;

    if ($_state >= _STATE_CONFIG_LOADED) {
        eval {
            $_jnx->load_configuration(rollback => 0);
        };
    }

    if ($_state >= _STATE_LOCKED)  {
        eval {
            $_jnx->unlock_configuration();
        };
    }

    if ($_state >= _STATE_CONNECTED)  {
        eval {
            $_jnx->request_end_session();
            $_jnx->disconnect();
        }
    }
}

#####
#
# Method: execute_configuration_change
# Description:
#   Make the configuration changes in a Juniper router.
# Input: xmlString => configuration in XML format
# Output: <none>
#
#####
sub execute_configuration_change
{
    my ($_self, $_xmlString) = @_;
    my (%_jnxInfo) = (
        'access' => $_self->{'jnxLSPConf'}->{'pss_access'},
        'login'  => $_self->{'jnxLSPConf'}->{'pss_login'},
        'password' => $_self->{'jnxLSPConf'}->{'pss_password'},
        'hostname' => $_self->{'lsp_from'}
    );
    my ($_xmlDoc);
    my ($_jnxRes, $_error, $_jnx);

    # Clear error message.
    $_self->{'errMsg'} = 0;

    # Initialize the XML Parser.
    my ($_xmlParser) = new XML::DOM::Parser;

    # Connect to the JUNOScript server.
    eval {
        ($_jnx) = new JUNOS::Device(%_jnxInfo);
        unless (ref $_jnx) {
            $_self->{'errMsg'} = "ERROR: $_jnxInfo{hostname}: failed to connect.\n";
            return();
        }
    };
    if ($@) {
        print STDERR "ignoring exception $@\n";
        return;
    }

    # Lock the Junoscript configuration database before making any changes
    $_jnxRes = $_jnx->lock_configuration();
    $_error = $_jnxRes->getFirstError();
    if ($_error)  {
        $_self->{'errMsg'} = "ERROR: $_jnxInfo{hostname}: failed to lock configuration.  Reason: $_error->{message}.\n";
        graceful_shutdown($_jnx, _STATE_CONNECTED);
        return();
    }

    # Load the Junoscript configuration.
    $_xmlDoc = $_xmlParser->parsestring($_xmlString);
    unless (ref($_xmlDoc)) {
        $_self->{'errMsg'} = "ERROR: Cannot parse $_xmlString, check to make sure the XML data is well-formed\n";
        graceful_shutdown($_jnx, _STATE_LOCKED);
        return();
    }

    # Put the load_configuration in an eval block to make sure if the rpc-reply
    # has any parsing errors, the grace_shutdown will still take place.  Do
    # not leave the database in an exclusive lock state.
    eval {
        $_jnxRes = $_jnx->load_configuration(
                     'format' => $_configFormat,
                     'action' => $_loadAction,
                     'configuration' => $_xmlDoc);
    };
    if ($@) {
        $_self->{'errMsg'} = "ERROR: Failed to load the configuration.   Reason: $@\n";
        graceful_shutdown($_jnx, _STATE_CONFIG_LOADED);
        return();
    }

    unless (ref($_jnxRes))  {
        $_self->{'errMsg'} = "ERROR: Failed to load the configuration\n";
        graceful_shutdown($_jnx, _STATE_LOCKED);
        return();
    }

    $_error = $_jnxRes->getFirstError();
    if ($_error)  {
        $_self->{'errMsg'} = "ERROR: Failed to load the configuration.  Reason: $_error->{message}\n";
        graceful_shutdown($_jnx, _STATE_CONFIG_LOADED);
        return();
    }

    # Commit the change.
    $_jnxRes = $_jnx->commit_configuration();
    $_error = $_jnxRes->getFirstError();
    if ($_error)  {
        $_self->{'errMsg'} = "ERROR: Failed to commit configuration.  Reason: $_error->{message}.\n";
        graceful_shutdown($_jnx, _STATE_CONFIG_LOADED);
    }
  
    # Configuration successfully commited.
    graceful_shutdown($_jnx, _STATE_LOCKED);
    return();
}

#####
#
# Method: execute_operational_command
# Description:
#   Perform operational command in a Juniper router.
# Input: router => router to execute command on
#        command => command to execute
# Output: 0 => failure
#         results of query
#
#####
sub execute_operational_command
{
    my ($_self, $_command) = @_;
    my (%_jnxInfo) = (
        'access' => $_self->{'jnxLSPConf'}->{'pss_access'},
        'login'  => $_self->{'jnxLSPConf'}->{'pss_login'},
        'password' => $_self->{'jnxLSPConf'}->{'pss_password'},
        'hostname' => $_self->{'lsp_from'}
    );
    my ($_jnxRes, $_error);
    my (%_queryArgs) = ('detail' => 0);  # 1 => "extensive" or "detail" view.

    # Clear error message.
    $_self->{'errMsg'} = 0;

    # Connect to the JUNOScript server.
    my ($_jnx) = new JUNOS::Device(%_jnxInfo);
    unless (ref $_jnx) {
        $_self->{'errMsg'} = "ERROR: $_jnxInfo{hostname}: failed to connect.\n";
        return(0);
    }

    # Send the command and receive a XML::DOM object.
    # jrl my $_jnxRes = $_jnx->$_command( %_queryArgs );
    $_jnxRes = $_jnx->$_command( %_queryArgs );
    unless (ref($_jnxRes))  {
        die "ERROR: $_jnxInfo{hostname}: failed to execute command $_command.\n";
    }

    # Check and see if there were any errors in executing the command.
    $_error = $_jnxRes->getFirstError();
    if ($_error)  {
        # jrl $_self->{'errMsg'} = "ERROR: $_jnxInfo{'hostname'} - ", $_error->{message}, "\n";
        $_self->{'errMsg'} = "ERROR: $_jnxInfo{'hostname'} - " . $_error->{message} . "\n";
        return(0);
    }
    return($_jnxRes->toString());
}


sub update_log {
    my( $self, $r, $xmlString) = @_;

    $r->{'reservation_tag'} =~ s/@/../;
    open (LOGFILE, ">> $ENV{'OSCARS_HOME'}/logs/$r->{'reservation_tag'}") || die "Can't open log file.\n";
    print LOGFILE "**********************************************************************\n";
    print LOGFILE $xmlString;
    close(LOGFILE);
}


1;
