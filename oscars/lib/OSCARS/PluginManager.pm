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

May 24, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use XML::Simple;

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

    $self->{config} = XMLin( $self->{location} );
} #____________________________________________________________________________


###############################################################################
# getConfiguration:  returns configuration.
#
sub getConfiguration {
    my( $self ) = @_;

    return $self->{config};
} #____________________________________________________________________________

###############################################################################
# usePlugin:  use given package, given a plugin name.
#
sub usePlugin {
    my( $self, $pluginName, $user ) = @_;

    my $database;

    my $plugin = $self->{config}->{plugin}->{$pluginName};
    my $packageName = $plugin->{location};
    if ( $plugin->{database} ) { 
        $database =
	        $self->{config}->{database}->{$plugin->{database}}->{location};
    }
    else {
        $database = $self->{config}->{database}->{'system'}->{location};
    }
    my $location = $packageName . '.pm';

    $location =~ s/(::)/\//g;
    eval { require $location };
    if (!$@) {
        return $packageName->new( 'database' => $database,
                                  'pluginMgr' => $self,
			          'user' => $user);
    }
    else { return undef; }
} #____________________________________________________________________________


######
1;
