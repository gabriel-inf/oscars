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

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

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

    return $self->listInstitutions($self->{db});
} #____________________________________________________________________________


###############################################################################
# listInstitutions:  Get names of all institutions associated with OSCARS users
#                    Used directly by other AAA SOAP methods.
#
sub listInstitutions {
    my( $self, $db ) = @_;

    my $statement = "SELECT name FROM institutions";
    my $results = $db->doQuery($statement);
    return $results;
} #____________________________________________________________________________


###############################################################################
# getId:  Get institution id given the institution name
#
sub getId {
    my( $self, $db, $institutionName ) = @_;

    my $statement = "SELECT id FROM institutions WHERE name = ?";
    my $row = $db->getRow($statement, $institutionName);
    if ( !$row ) {
        throw Error::Simple("The organization " .
                   "$institutionName is not in the database.");
    }
    return $row->{id};
} #____________________________________________________________________________


###############################################################################
# getName:  Get institution name given the institution id
#
sub getName {
    my( $self, $db, $institutionId ) = @_;

    my $statement = "SELECT name FROM institutions WHERE id = ?";
    my $row = $db->getRow($statement, $institutionId);
    if ( !$row ) {
        throw Error::Simple("The organization identified by " .
                   "$institutionId is not in the database.");
    }
    return $row->{name};
} #____________________________________________________________________________


######
1;

