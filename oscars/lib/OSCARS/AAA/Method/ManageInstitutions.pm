#==============================================================================
package OSCARS::AAA::Method::ManageInstitutions;

=head1 NAME

OSCARS::AAA::Method::ManageInstitutions - handles operations on institutions table.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::ManageInstitutions;

=head1 DESCRIPTION

Library for operations on institutions table (currently only retrieving).

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

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
# queryInstitutions:  Get names of all institutions associated with OSCARS users
#                   Used by other AAA SOAP methods.
#
sub queryInstitutions {
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

