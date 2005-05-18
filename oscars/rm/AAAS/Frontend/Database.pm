package AAAS::Frontend::Database;

# Database.pm:  package for AAAS database settings
# Last modified: May 9, 2005
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


##### Settings Begin (Global variables) #####

our( %table );

# database field names
# usage: @{ $table{'users'} }{'dn', 'password', 'level'}
%table = (
  'users' => {
      'id' => 'user_id',
      'last_name' => 'user_last_name',
      'first_name' => 'user_first_name',
      'dn' => 'user_dn',
      'password' => 'user_password',
      'email_primary' => 'user_email_primary',
      'level' => 'user_level',
      'email_secondary' => 'user_email_secondary',
      'phone_primary' => 'user_phone_primary',
      'phone_secondary' => 'user_phone_secondary',
      'description' => 'user_description',
      'register_time' => 'user_register_time',
      'activation_key' => 'user_activation_key',
      'institution_id' => 'institution_id',
  }
);

##### Settings End #####

sub get_AAAS_table
{
    my ( $self, $table_name ) = @_;
    return(%table);
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
                 $inref->{'dn'},
                 $inref->{'password'},
                 \%attr)
        }
        else { return( "You must log in first before accessing the database"); }
     
    }
    if (!$self->{'dbh'}) { return( "Unable to make database connection: $DBI::errstr"); }
    return "";
}


# Don't touch the line below
1;
