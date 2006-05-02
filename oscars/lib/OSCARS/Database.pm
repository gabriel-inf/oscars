#==============================================================================
package OSCARS::Database;

=head1 NAME

OSCARS::Database - Handles database connection and basic database requests.

=head1 SYNOPSIS

  use OSCARS::Database;

=head1 DESCRIPTION

This module contains methods for handling database connections and queries.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 1, 2006

=cut


use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 


###############################################################################
#
sub connect {
    my( $self, $databaseName ) = @_;

    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    $self->{dsn} = "DBI:mysql:" . $databaseName .
                   ";mysql_read_default_file=$ENV{HOME}/.my.cnf";
    $self->{dbh} = DBI->connect( $self->{dsn}, undef, undef, \%attr );
    if (!$self->{dbh}) {
        throw Error::Simple( "Unable to make database connection: $DBI::errstr");
    }
} #____________________________________________________________________________


###############################################################################
#
sub disconnect {
    my( $self ) = @_;

    $self->{dbh}->disconnect();
    $self->{dbh} = undef;
} #____________________________________________________________________________


###############################################################################
# doSelect:  returns all results from a select
#        
sub doSelect {
    my( $self, $statement, @args ) = @_;

    my $sth = $self->execStatement($statement, @args);
    my $rows = $sth->fetchall_arrayref({});
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Fetching results of $statement:  $DBI::errstr");
    }
    if ( !@$rows ) { return undef; }
    return $rows;
} #____________________________________________________________________________


###############################################################################
# getRow:  returns one row from a select (only one expected, TODO:  more error
#          checking)
#        
sub getRow {
    my( $self, $statement, @args ) = @_;

    my $sth = $self->execStatement($statement, @args);
    my $rows = $sth->fetchall_arrayref({});
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Fetching results of $statement:  $DBI::errstr");
    }
    if ( !@$rows ) { return undef; }
    return $rows->[0];
} #____________________________________________________________________________


###############################################################################
#  execStatement:  performs any DB operation not requiring a fetch
#
sub execStatement {
    my( $self, $statement, @args ) = @_;

    my $sth = $self->{dbh}->prepare( $statement );
    if ($DBI::err) {
        throw Error::Simple("[DBERROR] Preparing $statement:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Error::Simple("[DBERROR] Executing $statement:  $DBI::errstr");
    }
    return $sth;
} #____________________________________________________________________________


###############################################################################
#
sub getPrimaryId {
    my( $self ) = @_;

    return $self->{dbh}->{mysql_insertid};
} #____________________________________________________________________________


######
1;
