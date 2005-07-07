package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database handling
#               inherits from Common::Database
# Last modified: June 30, 2005
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
        $results->{error_msg} = 'Already logged out.';
        return ( $results );
    }
    if ($user_dn ne 'unpriv') {
        $self->update_user_status($user_dn, 'Logged out');
    }
    if (!$self->{handles}->{$user_dn}->disconnect()) {
        $results->{error_msg} = "Could not disconnect from database";
        return ( $results );
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

    my( $query, $sth, $err_msg );

    $query = "UPDATE users SET user_status = ? WHERE user_dn = '$user_dn'";
    ($sth, $err_msg) = $self->do_query($user_dn, $query, $status);
    if ($err_msg) { return $err_msg; }
    return ( "" );
}
######

###############################################################################
# enforce_connection:  Checks to see if user has logged in.
#
sub enforce_connection {
    my ( $self, $user_dn ) = @_;

    my ( $query, $sth, $err_msg );

    $query = "SELECT user_status FROM users WHERE user_dn = ?";
    ($sth, $err_msg) = $self->do_query('', $query, $user_dn);
    if ($err_msg) { return $err_msg; }

    if (!$sth->rows) {
        $sth->finish();
        $err_msg = 'Please check your login name and try again.';
        return $err_msg;
    }
    my $ref = $sth->fetchrow_hashref();
    if (!$ref->{user_status} || ($ref->{user_status} eq 'Logged out')) {
        return( "You must log in first before accessing the database");
    }
    #elsif (!$self->{handles}->{$user_dn}) {
        #$err_msg = $self->login_user($user_dn);
        #if ($err_msg) { return( 1, $err_msg) }
    #}
    # for now, handle set up per connection
    $err_msg = $self->login_user($user_dn);
    if ($err_msg) {
        print STDERR "login_user error:  $err_msg\n";
        return($err_msg);
    }
    return "";
}
######

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
