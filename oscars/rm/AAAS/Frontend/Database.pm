package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database handling
#               inherits from Common::Database
# Last modified: July 8, 2005
# Soo-yeon Hwang (dapi@umich.edu)
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
sub login_user {
    my( $self, $user_dn ) = @_;

    #print STDERR "Login called with $user_dn\n";
    $self->Common::Database::login_user($user_dn);
    if ($user_dn ne 'unpriv') {
        $self->update_user_status($user_dn, 'Logged in');
    }
}
######

###############################################################################
sub logout {
    my( $self, $user_dn ) = @_;

    my $results = {};
    if (!$self->{handles}->{$user_dn}) {
        throw Common::Exception("Already logged out.");
    }
    if ($user_dn ne 'unpriv') {
        $self->update_user_status($user_dn, 'Logged out');
    }
    if (!$self->{handles}->{$user_dn}->disconnect()) {
        throw Common::Exception("Could not disconnect from database");
    }
    if ($user_dn ne 'unpriv') {
        $self->{handles}->{$user_dn} = undef;
    }
    return ( $results );
}
######

###############################################################################
sub update_user_status {
    my( $self, $user_dn, $status ) = @_;

    my( $query, $sth );

    $query = "UPDATE users SET user_status = ? WHERE user_dn = '$user_dn'";
    $sth = $self->do_query($user_dn, $query, $status);
    return;
}
######

###############################################################################
# enforce_connection:  Checks to see if user has logged in.
#
sub enforce_connection {
    my ( $self, $user_dn ) = @_;

    my ( $query, $sth );

    $query = "SELECT user_status FROM users WHERE user_dn = ?";
    $sth = $self->do_query('', $query, $user_dn);

    if (!$sth->rows) {
        $sth->finish();
        throw Common::Exception("Please check your login name and try again.");
    }
    my $ref = $sth->fetchrow_hashref();
    if (!$ref->{user_status} || ($ref->{user_status} eq 'Logged out')) {
        throw Common::Exception("You must log in first before accessing the database.");
    }
    # for now, handle set up per connection
    $self->login_user($user_dn);
    return;
}
######

###############################################################################
#
sub get_user_levels {
    my( $self, $user_dn ) = @_;

    my( %levels, $r, $sth, $query );

    $query = "SELECT user_level_bit, user_level_description FROM user_levels";
    $sth = $self->do_query($user_dn, $query);
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
    $sth = $self->do_query($user_dn, $query, $inref->{institution});
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
