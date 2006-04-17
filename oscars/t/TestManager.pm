# =============================================================================
package TestManager;

=head1 NAME

TestManager - Perl test manager.

=head1 SYNOPSIS

  use TestManager;

=head1 DESCRIPTION

Handles tests.  TODO:  Merge with Test::Harness runtests, and have just one
instance of TestManager for all tests.

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

use OSCARS::Database;
use OSCARS::PluginManager;
use OSCARS::ClientManager;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
#
sub get_params {
    my( $self, $fname ) = @_;

    $self->{plugin_mgr} = OSCARS::PluginManager->new();
    $self->{db} = OSCARS::Database->new();
    my $params = {};
    my $parser = new XML::DOM::Parser;
    my $doc = $parser->parsefile( $fname );
    my $nodes = $doc->getElementsByTagName( "param" );
    my $n = $nodes->getLength;
    for (my $i = 0; $i < $n; $i++) {
        my $node = $nodes->item ($i);
        my $attr = $node->getAttributeNode( "name" );
	# TODO:  error checking
        if ($attr) {
	    my $name = $attr->getValue;
            $attr = $node->getAttributeNode( "value" );
	    $params->{$name} = $attr->getValue;
	}
    }
    $self->{database} = $self->{plugin_mgr}->get_database($params->{component});
    $self->{db}->connect($self->{database});
    my $authN = $self->{plugin_mgr}->use_plugin('authentication');
    my $credentials = $authN->get_credentials($params->{user_login},
                                              'password');
    $params->{user_password} = $credentials;
    return $params;
} #____________________________________________________________________________


###############################################################################
#
sub dispatch {
    my( $self, $params ) = @_;

    if ( !$self->{client_mgr} ) {
	my $database = $self->{plugin_mgr}->get_database('Intradomain');
        $self->{client_mgr} = OSCARS::ClientManager->new(
	                                       'database' => $database);
    }
    my $som = $self->{client_mgr}->get_client()->dispatch($params);
    return $som;
} #____________________________________________________________________________


###############################################################################
# Only used by intradomain tests.
#
sub get_intradomain_configs {
    my( $self, $test_name ) = @_;

    my $statement = "SELECT * FROM $self->{database}.test_addresses a " .
        "INNER JOIN $self->{database}.test_confs t ON a.test_conf_id = t.test_conf_id " .
        'WHERE t.test_name = ?';
    my $rows = $self->{db}->do_query($statement, $test_name);
    my $configs = {};
    for my $row (@$rows) {
        $configs->{$row->{test_address_description}} = $row->{test_address};
    }
    return $configs;
} #____________________________________________________________________________


