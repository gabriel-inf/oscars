#==============================================================================
package OSCARS::Interdomain::Request;

=head1 NAME

OSCARS::Interdomain::Request - Forward a request to another domain.

=head1 SYNOPSIS

  use OSCARS::Interdomain::Request;

=head1 DESCRIPTION

Forward a request to another domain (currently only OSCARS/BRUW).

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::ClientManager;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

} #____________________________________________________________________________


sub forward {
    my( $self, $params, $database ) = @_;

    print STDERR "next domain: $params->{next_domain}\n";
    my $client_mgr = OSCARS::ClientManager->new('database' => $database);
    my $client = $client_mgr->get_client($params->{next_domain});
    if ( !$client ) {
        return( 'Unable to get client for next domain', undef );
    }
    my $payload = {};
    $payload->{component} = 'Interdomain';
    $payload->{method} = 'Forward';
    $payload->{params} = $params;
    # TODO:  FIX hard wiring
    $payload->{user_login} = 'xdomain';
    $payload->{user_password} = 'crosstest';
    my $som = $client->dispatch($payload);
    if ( !$som ) { return( 'Unable to make forwarding SOAP call', undef ); }
    if ($som->faultstring) { return( $som->faultstring, undef ); }
    return( undef, $som->result );
}


######
1;
# vim: et ts=4 sw=4
