package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database handling
#               inherits from Common::Database
# Last modified: November 5, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use Common::Exception;
use Common::Database;

our @ISA = qw(Common::Database);

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    # call super-classes initialize method
    $self->initialize();
    return( $self );
}


###############################################################################
#
sub get_institution_id {
    my( $self, $inref ) = @_;

    my $query = "SELECT institution_id FROM institutions
                WHERE institution_name = ?";
    my $rows = $self->do_query($inref->{institution});
    if (!@$rows) {
        throw Common::Exception("The organization " .
                   "$inref->{institution} is not in the database.");
    }
    $inref->{institution_id} = $rows->[0]->{institution_id} ;
    return;
}
######

# Don't touch the line below
1;
