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

    $self->{config} = $self->readConfiguration();
} #____________________________________________________________________________


###############################################################################
# readConfiguration:  read and parse XML configuration file.
#
sub readConfiguration {
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
# usePlugin:  use given package, given a plugin name.
#
sub usePlugin {
    my( $self, $pluginName ) = @_;

    my $packageName = $self->{config}->{$pluginName}->{package};
    my $database = $self->{config}->{$pluginName}->{database};
    my $location = $packageName . '.pm';

    $location =~ s/(::)/\//g;
    eval { require $location };
    if (!$@) {
        my $newInstance = $packageName->new('database' => $database);
	return $newInstance;
    }
    else { return undef; }
} #____________________________________________________________________________


###############################################################################
# getDatabase:  get associated database name, given a plugin name.
#
sub getDatabase {
    my( $self, $pluginName ) = @_;

    return $self->{config}->{$pluginName}->{database};
} #____________________________________________________________________________


###############################################################################
# getPackage:  get package name (or prefix), given a plugin name.
#
sub getPackage {
    my( $self, $pluginName ) = @_;

    return $self->{config}->{$pluginName}->{package};
} #____________________________________________________________________________


######
1;
