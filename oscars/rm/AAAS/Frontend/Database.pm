package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database settings
# Last modified: May 18, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;

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

sub initialize {
    my ( $_self ) = @_;
    # hash holds database handle for each connected user
    $_self->{handles} = {};
}
######

###############################################################################
#
sub check_connection
{
    my ( $self, $user_dn, $do_login, $reconnect ) = @_;
    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    # TODO:  FIX, may need views, or different db logins for one thing
    if ($reconnect || !(defined($self->{handles}->{$user_dn}))) {
        if ($do_login) {
            $self->{handles}->{$user_dn} = DBI->connect(
                 $self->{configs}->{use_AAAS_database}, 
                 $self->{configs}->{AAAS_login_name}, 
                 'ritazza6',
                 \%attr)
        }
        else {
            return( "You must log in first before accessing the database");
        }
    }
    if (!$self->{handles}->{$user_dn}) {
        return( "Unable to make database connection: $DBI::errstr");
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

###############################################################################
#
sub do_query
{
    my( $self, $user_dn, $query, @args ) = @_;
    my( $sth, $error_msg );

    $sth = $self->{handles}->{$user_dn}->prepare( $query );
    if ($DBI::err) {
        $error_msg = "[DBERROR] Preparing $query:  $DBI::errstr";
        return (undef, $error_msg);
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        $error_msg = "[DBERROR] Executing $query:  $DBI::errstr";
        $sth->finish();
        return(undef, $error_msg);
    }
    return( $sth, '');
}
######

###############################################################################
sub logout {
    my( $self, $user_dn ) = @_;

    my $results = {};
    if (!$self->{handles}->{$user_dn}) {
        $results->{status_msg} = 'Already logged out.';
        return ( 0, $results );
    }
    if (!$self->{handles}->{$user_dn}->disconnect()) {
        $results->{error_msg} = "Could not disconnect from database";
        return ( 1, $results );
    }
    $self->{handles}->{$user_dn} = undef;
    $results->{status_msg} = 'Logged out';
    return ( 0, $results );
}
######


# Don't touch the line below
1;
