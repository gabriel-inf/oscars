package BSS::Frontend::DBSettings;

# DBSettings.pm:  BSS specific database settings
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw($Dbname %Table %Table_field);

##### Settings Begin (Global variables) #####
# database connection info
$Dbname = 'BSS';

# database table names
%Table = (
  'reservations' => 'reservations',
);

# reservations field names
%Table_field = (
  'reservations' => {
    'id' => 'reservation_id',
    'start_time' => 'reservation_start_time',
    'end_time' => 'reservation_end_time',
    'qos' => 'reservation_qos',
    'status' => 'reservation_status',
    'description' => 'reservation_description',
    'created_time' => 'reservation_created_time',
    'ingress_port' => 'reservation_ingress_port',
    'egress_port' => 'reservation_egress_port',
    'ingress_interface_id' => 'ingress_interface_id',
    'egress_interface_id' => 'egress_interface_id',
    'user_dn' => 'user_dn',
  }
);


##### Settings End #####

# Don't touch the line below
1;
