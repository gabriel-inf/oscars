package Common::Database;

# Database.pm:  superclass for AAAS and BSS database handling
# Last modified: November 5, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use strict;

use DBI;
use Data::Dumper;
use Error;
use Common::Exception;

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
    if ($self->{dbh}) {
        $self->{dbh}->disconnect();
    }

    # Start with a fresh handle.
    $self->{dbh} = DBI->connect(
                 $self->{database}, 
                 $self->{dblogin}, 
                 $self->{password},
                 \%attr);
    if (!$self->{dbh}) {
        throw Common::Exception( "Unable to make database connection: $DBI::errstr");
    }
    return;
}
######

###############################################################################
# get_debug_level:  gets current debug level (0: off, 1: on)
#
sub get_debug_level {
    my( $self ) = @_;

    my( $sth, $query );

        # get default for now
    $query = 'SELECT server_debug ' .
             'FROM servers WHERE server_id = 1';
    $sth = $self->do_query('', $query);
    if (!$sth->rows) {
        $sth->finish();
        throw Common::Exception("Could not find debug level.");
    }
    my $ref = $sth->fetchrow_hashref();
    $sth->finish();
    return( $ref->{server_debug} );
}
######

###############################################################################
#
sub do_query {
    my( $self, $query, @args ) = @_;

    my $sth = $self->{dbh}->prepare( $query );
    if ($DBI::err) {
        throw Common::Exception("[DBERROR] Preparing $query:  $DBI::errstr");
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        throw Common::Exception("[DBERROR] Executing $query:  $DBI::errstr");
    }
    return( $sth );
}
######

# Don't touch the line below
1;
