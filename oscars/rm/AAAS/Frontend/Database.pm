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
    $_self->{dbh} = undef;
}
######

###############################################################################
#
sub check_connection
{
    my ( $self, $inref, $reconnect ) = @_;
    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    # TODO:  FIX
    if (!$self->{dbh} || $reconnect) {
        if ($inref) {
            $self->{dbh} = DBI->connect(
                 $self->{configs}->{use_AAAS_database}, 
                 $self->{configs}->{AAAS_login_name}, 
                 'ritazza6',
                 #$inref->{user_dn},
                 #$inref->{user_password},
                 \%attr)
        }
        else {
            return( "You must log in first before accessing the database");
        }
     
    }
    if (!$self->{dbh}) {
        return( "Unable to make database connection: $DBI::errstr");
    }
    return "";
}
######

###############################################################################
#
sub get_user_levels {
    my( $self ) = @_;

    my( %levels, $r, $sth, $query, $error_msg );

    $query = "SELECT user_level_bit, user_level_description FROM user_levels";
    ($sth, $error_msg) = $self->do_query($query);
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
    my( $self, $query, @args ) = @_;
    my( $sth, $error_msg );

    $sth = $self->{dbh}->prepare( $query );
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


# Don't touch the line below
1;
