# =============================================================================
package OSCARS::PluginManager;

=head1 NAME

OSCARS::PluginManager - plugin manager for OSCARS.

=head1 SYNOPSIS

  use OSCARS::PluginManager;

=head1 DESCRIPTION

Handles plugins associated with OSCARS, given an XML configuration file.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use XML::DOM;

use Data::Dumper;
use Error qw(:try);

use strict;

use SOAP::Lite;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{config} = $self->read_configuration_file();
} #____________________________________________________________________________


###############################################################################
# read_configuration_file:  read and parse XML configuration file.
#
sub read_configuration_file {
    my( $self ) = @_;

    my $conf = {};
    my $parser = new XML::DOM::Parser;
    my $doc = $parser->parsefile( "$ENV{HOME}/.oscars.xml" );
    # print all attributes of all plugin elements
    my $nodes = $doc->getElementsByTagName( "plugin" );
    my $n = $nodes->getLength;
    for (my $i = 0; $i < $n; $i++) {
        my $node = $nodes->item ($i);
        my $attr = $node->getAttributeNode( "name" );
	# TODO:  error checking
        if ($attr) {
	    my $name = $attr->getValue;
            $conf->{$name} = {};
            $attr = $node->getAttributeNode( "package" );
	    $conf->{$name}->{package} = $attr->getValue;
            $attr = $node->getAttributeNode ( "database" );
            $conf->{$name}->{database} = $attr->getValue;
	}
    }
    return $conf;
} #____________________________________________________________________________


###############################################################################
# use_plugin:  use given package, given a plugin name.
#
sub use_plugin {
    my( $self, $plugin_name ) = @_;

    my $package_name = $self->{config}->{$plugin_name}->{package};
    my $database = $self->{config}->{$plugin_name}->{database};
    my $location = $package_name . '.pm';

    $location =~ s/(::)/\//g;
    eval { require $location };
    if (!$@) {
        my $new_instance = $package_name->new('database' => $database);
	return $new_instance;
    }
    else { return undef; }
} #____________________________________________________________________________


###############################################################################
# get_database:  get associated database name, given a plugin name.
#
sub get_database {
    my( $self, $plugin_name ) = @_;

    return $self->{config}->{$plugin_name}->{database};
} #____________________________________________________________________________


###############################################################################
# get_package:  get package name (or prefix), given a plugin name.
#
sub get_package {
    my( $self, $plugin_name ) = @_;

    return $self->{config}->{$plugin_name}->{package};
} #____________________________________________________________________________


######
1;
