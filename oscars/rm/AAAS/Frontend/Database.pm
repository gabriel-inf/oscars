package AAAS::Frontend::Database;

# Database.pm:  package for AAAS specific database settings
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw($Dbname %Table %Table_field);


##### Settings Begin (Global variables) #####

$Dbname = 'AAAS';

# database table names
%Table = (
  'users' => 'users',
);

# database field names
# usage: @{ $table_field{'users'} }{'dn', 'password', 'level'}
%Table_field = (
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

# Don't touch the line below
1;
