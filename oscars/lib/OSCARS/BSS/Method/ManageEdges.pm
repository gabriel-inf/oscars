###############################################################################
package OSCARS::BSS::Method::ManageEdges;

=head1 NAME

OSCARS::BSS::Method::ManageEdges - SOAP method handling domain edge information.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::ManageEdges;

=head1 DESCRIPTION

SOAP method handling the exchange of OSCARS routers, interfaces, and ipaddrs 
table information with a neighboring domain running OSCARS.  Each server can 
initiate a request to the other by providing its own edge information, and get 
back the corresponding edge information in the neighboring domain.  This
class inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 21, 2006

=cut


use strict;

use File::Basename;
use Data::Dumper;
use Error qw(:try);

use OSCARS::User;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soap_method:
#
# In:  reference to hash of parameters
# Out: edge information hash
#
sub soap_method {
    my( $self ) = @_;

    my $results;

    $results = $self->get_edges($self->{user}, $self->{params});
    return $results;
} #____________________________________________________________________________


###############################################################################
#
sub get_edges {
    my( $self, $user, $params ) = @_;
 
    my( $inner_results, $statement, @ipaddrs, @routers );

    my $results = {};
    if (1) {
    #if ( $params->{domain_str} ) {
        $statement = 'SELECT * FROM BSS.interfaces WHERE ' .
                     "interface_alias like '%$params->{domain_str}%'";
    }
    $results->{interfaces} = $user->do_query($statement);
    if (!$results->{interfaces}) { return $results; }

    for my $row (@{$results->{interfaces}}) {
        $statement = 'SELECT * FROM BSS.ipaddrs WHERE ' .
                        'interface_id = ?';
        $inner_results = $user->get_row($statement, $row->{interface_id});
	    push(@ipaddrs, $inner_results);
        $statement = 'SELECT * FROM BSS.routers WHERE ' .
                        'router_id = ?';
        $inner_results = $user->get_row($statement, $row->{router_id});
	    push(@routers, $inner_results);
    }
    $results->{routers} = \@routers; 
    $results->{ipaddrs} = \@ipaddrs; 
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
