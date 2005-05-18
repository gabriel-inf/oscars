package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database settings
# Last modified: May 18, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;

######################################################################
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


######################################################################
sub initialize {
    my ( $_self ) = @_;
    $_self->{'dbh'} = undef;
}


sub check_connection
{
    my ( $self, $inref ) = @_;
    my ( %attr ) = (
        RaiseError => 0,
        PrintError => 0,
    );
    if (!$self->{'dbh'}) {
        if ($inref) {
            $self->{'dbh'} = DBI->connect(
                 $self->{'configs'}->{'use_AAAS_database'}, 
                 $inref->{'user_dn'},
                 $inref->{'user_password'},
                 \%attr)
        }
        else { return( "You must log in first before accessing the database"); }
     
    }
    if (!$self->{'dbh'}) { return( "Unable to make database connection: $DBI::errstr"); }
    return "";
}


# Don't touch the line below
1;
