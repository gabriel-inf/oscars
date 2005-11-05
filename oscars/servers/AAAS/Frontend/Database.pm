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
sub new {
    my $invocant = shift;
    my $_class = ref($invocant) || $invocant;
    my ($_self) = {@_};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

###############################################################################
#
sub get_user_levels {
    my( $self, $user_dn ) = @_;

    my( %levels, $r, $sth, $query );

    $query = "SELECT user_level_bit, user_level_description FROM user_levels";
    $sth = $self->do_query($query);
    my $rows = $sth->fetchall_arrayref();
    for $r (@$rows) { $levels{$$r[1]} = $$r[0]; }
    $levels{'inactive'} = 0;
    return( \%levels );
}
######

###############################################################################
#
sub get_institution_id {
    my( $self, $inref, $user_dn ) = @_;

    my( $sth, $query );

    $query = "SELECT institution_id FROM institutions
              WHERE institution_name = ?";
    $sth = $self->do_query($inref->{institution});
    if (!$sth->rows) {
        $sth->finish();
        throw Common::Exception("The organization " .
                   "$inref->{institution} is not in the database.");
    }
    my $ref = $sth->fetchrow_hashref;
    $inref->{institution_id} = $ref->{institution_id} ;
    $sth->finish();
    return;
}
######

# Don't touch the line below
1;
