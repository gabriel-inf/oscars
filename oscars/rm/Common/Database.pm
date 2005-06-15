package Common::Database;

# Database.pm:  superclass for AAAS and BSS database handling
# Last modified: June 14, 2005
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
# enforce_connx:  Sets up database handle for user, or denies access if
#                     necessary
#
sub enforce_connx
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
                 $self->{database}, 
                 $self->{login}, 
                 $self->{password},
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
sub do_query
{
    my( $self, $user_dn, $query, @args ) = @_;
    my( $sth, $error_msg );

    #$print STDERR "** do_query:  $user_dn $query\n";
    $sth = $self->{handles}->{$user_dn}->prepare( $query );
    if ($DBI::err) {
        $error_msg = "[DBERROR] Preparing $query:  $DBI::errstr";
        print STDERR "DBERROR ", $error_msg, "\n";
        return (undef, $error_msg);
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        $error_msg = "[DBERROR] Executing $query:  $DBI::errstr";
        print STDERR "DBERROR ", $error_msg, "\n";
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
