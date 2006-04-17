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

February 15, 2006

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
    my( $self, $database_name ) = @_;

    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    $self->{dsn} = "DBI:mysql:" . $database_name .
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
sub get_primary_id {
    my( $self ) = @_;

    return $self->{dbh}->{mysql_insertid};
} #____________________________________________________________________________


######
1;
