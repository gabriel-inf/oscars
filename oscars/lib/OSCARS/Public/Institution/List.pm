#==============================================================================
package OSCARS::Public::Institution::List;

=head1 NAME

OSCARS::Public::Institution::List - Retrieves info in institutions table.

=head1 SYNOPSIS

  use OSCARS::Public::Method::Institution::List;

=head1 DESCRIPTION

Retrieves information from institutions table.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 25, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $self into designated class.
    bless($self, $class);
    return($self);
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Get names of all institutions associated with OSCARS users
#              Used by the "add user" form.
#
sub soapMethod {
    my( $self ) = @_;

    my $results = {};
    $results->{institutionList} = $self->listInstitutions($self->{db});
    return $results;
} #____________________________________________________________________________


###############################################################################
# listInstitutions:  Get names of all institutions associated with OSCARS users
#                    Used directly by other AAA SOAP methods.
#
sub listInstitutions {
    my( $self, $db ) = @_;

    my $statement = "SELECT name FROM institutions";
    my $results = $db->doSelect($statement);
    return $results;
} #____________________________________________________________________________


######
1;

