package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database handling
#               inherits from Common::Database
# Last modified: June 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;

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

    my( %levels, $r, $sth, $query, $error_msg );

    $query = "SELECT user_level_bit, user_level_description FROM user_levels";
    ($sth, $error_msg) = $self->do_query($user_dn, $query);
    if( $error_msg ) { return( undef, $error_msg ) };
    my $rows = $sth->fetchall_arrayref();
    for $r (@$rows) { $levels{$$r[1]} = $$r[0]; }
    $levels{'inactive'} = 0;
    return( \%levels, "" );
}
 
######

# Don't touch the line below
1;
