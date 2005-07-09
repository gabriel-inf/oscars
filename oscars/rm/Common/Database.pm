package Common::Database;

# Database.pm:  superclass for AAAS and BSS database handling
# Last modified: July 8, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

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
    # hash holds database handle for each connected user
    $self->{handles} = {};
}
######

###############################################################################
# login_user:  Sets up database handle for user
#
sub login_user {
    my ( $self, $user_dn ) = @_;

    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    # I couldn't find a foolproof way to check for timeout; Apache::DBI
    # came closest, but it was too dependent on the driver handling the timeout
    # correctly.  So instead,
    # if a handle is left over from a previous session, attempts to disconnect.
    # If it was timed out, the error is ignored.
    if ($self->{handles}->{$user_dn}) {
        $self->{handles}->{$user_dn}->disconnect();
    }

    # Start with a fresh handle.
    $self->{handles}->{$user_dn} = DBI->connect(
                 $self->{database}, 
                 $self->{login}, 
                 $self->{password},
                 \%attr);
    if (!$self->{handles}->{$user_dn}) {
        throw Common::Exception( "Unable to make database connection: $DBI::errstr");
    }
    return;
}
######

###############################################################################
#
sub do_query {
    my( $self, $user_dn, $query, @args ) = @_;

    if (!$user_dn) {
        $user_dn = 'unpriv';
        # Handle for unprivileged database access.
        # Makes sure there is a fresh handle for that pseudo-user.
        $self->login_user('unpriv');
    }
    my $sth = $self->{handles}->{$user_dn}->prepare( $query );
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
