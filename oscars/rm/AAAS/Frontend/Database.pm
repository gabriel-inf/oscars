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
    $_self->{'dbh'} = DBI->connect($_self->{'configs'}->{'db_use_database'}, 
             $_self->{'configs'}->{db_login_name}, $_self->{'configs'}->{'db_login_passwd'})
            or die "Couldn't connect to database: " . DBI->errstr;
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
      'email_secondary' => 'user_email_secondary',
      'phone_primary' => 'user_phone_primary',
      'phone_secondary' => 'user_phone_secondary',
      'description' => 'user_description',
      'level' => 'user_level',
      'register_time' => 'user_register_time',
      'activation_key' => 'user_activation_key',
      'pending_level' => 'user_pending_level',
      'authorization_id' => 'authorization_id',
      'institution_id' => 'institution_id',
  }
);

##### Settings End #####

sub get_AAAS_table
{
    my ( $self, $table_name ) = @_;
    return(%table);
}

# Don't touch the line below
1;
