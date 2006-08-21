#==============================================================================
package OSCARS::Database;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

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
