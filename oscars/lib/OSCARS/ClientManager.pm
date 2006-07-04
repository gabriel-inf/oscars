# =============================================================================
package OSCARS::ClientManager;

=head1 NAME

OSCARS::ClientManager - SOAP client creator.

=head1 SYNOPSIS

  use OSCARS::ClientManager;

=head1 DESCRIPTION

Creates SOAP clients, using cached information from the clients table in the
oscars database.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

June 14, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

use WSRF::Lite;
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

    $self->{configuration}->{namespace} =
                                      'http://oscars.es.net/OSCARS/Dispatcher';
} #____________________________________________________________________________


###############################################################################
# getClient:  Returns new WSRF::Lite client for given domain, with a SOAP
#             action set to the given method.
#
sub getClient {
    my( $self, $methodName, $domain ) = @_;

    if ( !$domain ) { $domain = 'default'; }
    if (!$self->{configuration}->{$domain}) {
        print STDERR "domain $domain not handled\n";
        return undef;
    }
    my $soapAction = $self->{configuration}->{namespace} . '/' . $methodName;
    my $client = WSRF::Lite
        -> uri( $self->{configuration}->{$domain}->{uri} )
        -> proxy( $self->{configuration}->{$domain}->{proxy} )
        -> on_action ( sub { return "$soapAction" } );
    return $client;
} #____________________________________________________________________________


###############################################################################
# getLogin:  Gets login name associated with other domain.  Requests are
#            forwarded as this (pseudo) user, which must be in the users
#            table in the local domain's database.
#
sub getLogin {
    my( $self, $domain ) = @_;

    if ( !$domain ) { return undef; }
    return $self->{configuration}->{$domain}->{payloadSender};
} #____________________________________________________________________________


######
1;
