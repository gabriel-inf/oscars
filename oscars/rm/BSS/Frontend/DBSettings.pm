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
    'created_time' => 'reservation_created_time',
    'bandwidth' => 'reservation_bandwidth',
    'resv_class' => 'reservation_class',
    'burst_limit' => 'reservation_burst_limit',
    'status' => 'reservation_status',
    'ingress_id' => 'ingress_interface_id',
    'egress_id' => 'egress_interface_id',
    'src_id' => 'src_hostaddrs_id',
    'dst_id' => 'dst_hostaddrs_id',
    'user_dn' => 'user_dn',
    'ingress_port' => 'reservation_ingress_port',
    'egress_port' => 'reservation_egress_port',
    'dscp' => 'reservation_dscp',
    'description' => 'reservation_description',
  }
);


##### Settings End #####

# Don't touch the line below
1;
