#==============================================================================
package OSCARS::AAAS::Method::ManageInstitutions;

=head1 NAME

OSCARS::AAAS::Method::ManageInstitutions - handles operations on institutions table.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ManageInstitutions;

=head1 DESCRIPTION

Library for operations on users table (selecting, adding, deleting).

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 10, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $_self into designated class.
    bless($self, $class);
    return($self);
} #____________________________________________________________________________


###############################################################################
# get_institutions:  Get names of all institutions associated with OSCARS users
#                    Used by other AAAS SOAP methods.
#
sub get_institutions {
    my( $self, $user ) = @_;

    my $statement = "SELECT institution_name FROM institutions";
    my $results = $user->do_query($statement);
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_id:  Get institution id given the institution name
#
sub get_id {
    my( $self, $user, $institution_name ) = @_;

    my $statement = "SELECT institution_id FROM institutions " .
                    "WHERE institution_name = ?";
    my $row = $user->get_row($statement, $institution_name);
    if ( !$row ) {
        throw Error::Simple("The organization " .
                   "$institution_name is not in the database.");
    }
    return $row->{institution_id};
} #____________________________________________________________________________


###############################################################################
# get_name:  Get institution name given the institution id
#
sub get_name {
    my( $self, $user, $institution_id ) = @_;

    my $statement = "SELECT institution_name FROM institutions " .
                    "WHERE institution_id = ?";
    my $row = $user->get_row($statement, $institution_id);
    if ( !$row ) {
        throw Error::Simple("The organization identified by " .
                   "$institution_id is not in the database.");
    }
    return $row->{institution_name};
} #____________________________________________________________________________


######
1;

