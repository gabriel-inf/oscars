###############################################################################
package AAAS::Frontend::Database;

# AAAS database request handling.
# Last modified:   November 21, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ( $self ) = @_;

    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    # I couldn't find a foolproof way to check for timeout; Apache::DBI
    # came closest, but it was too dependent on the driver handling the timeout
    # correctly.  So instead,
    # if a handle is left over from a previous session, attempts to disconnect.
    # If it was timed out, the error is ignored.
    # TODO:  FIX
    if ($self->{dbh}) {
        $self->{dbh}->disconnect();
    }
    $self->{dbh} = DBI->connect(
                 $self->{database}, 
                 $self->{dblogin}, 
                 $self->{password},
                 \%attr);
    if (!$self->{dbh}) {
        throw Error::Simple( "Unable to make database connection: $DBI::errstr");
    }
} #____________________________________________________________________________ 


# TODO:  FIX duplication
###############################################################################
#
sub do_query {
    my( $self, $statement, @args ) = @_;

    # TODO, FIX:  selectall_arrayref probably better
    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    my $rows = $sth->fetchall_arrayref({});
    #if ( $DBI::err ) {
        #throw Error::Simple("[DBERROR] Fetching results of $statement:  $DBI::errstr");
    #}
    return $rows;
} #____________________________________________________________________________ 


###############################################################################
#
sub get_row {
    my( $self, $statement, @args ) = @_;

    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    my $rows = $sth->fetchall_arrayref({});
    if ( !@$rows ) { return undef; }
    # TODO:  error checking if more than one row
    return $rows->[0];
} #____________________________________________________________________________ 


###############################################################################
#
sub get_institution_id {
    my( $self, $params ) = @_;

    my $statement = "SELECT institution_id FROM institutions
                WHERE institution_name = ?";
    my $row = $self->get_row($statement, $params->{institution});
    if ( !$row ) {
        throw Error::Simple("The organization " .
                   "$params->{institution} is not in the database.");
    }
    $params->{institution_id} = $row->{institution_id} ;
} #____________________________________________________________________________ 


######
1;
