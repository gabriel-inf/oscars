# Database.pm:  BSS specific database settings and routines
# Last modified: April 18, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee (jrlee@lbl.gov)

package BSS::Frontend::Database;

use strict; 

use DB;
use DBI;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw($Dbname %Table %Table_field @Table_field_order ipaddr_to_iface_idx hostaddr_to_idx);

##### Settings Begin (Global variables) #####
# database connection info
our $Dbname = 'BSS';

# database table names
our %Table = (
  'reservations' => 'reservations',
);

# reservations field names
our %Table_field = (
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
    'dn' => 'user_dn',
    'ingress_port' => 'reservation_ingress_port',
    'egress_port' => 'reservation_egress_port',
    'dscp' => 'reservation_dscp',
    'description' => 'reservation_description',
  }
);

our @Table_field_order = ('id', 'start_time', 'end_time', 'created_time',
    'bandwidth', 'class', 'burst_limit', 'status', 'ingress_id', 'egress_id',
    'src_id', 'dst_id', 'dn', 'ingress_port', 'egress_port', 'dscp',
    'description');

##### Settings End #####



#######################################################################
### Get the interface idx, given a router ip address
# IN:  ipaddrs_ip
# OUT: interface_idx
#######################################################################
sub ipaddr_to_iface_idx  {

  my ($ipaddrs_ip) = @_;
  my ($query, $error_msg, $sth, $dbh);
  my ($interface_idx);

  ( $error_msg, $dbh ) = database_connect($Dbname);
  if ( $error_msg ) { return(0); }


     # TODO:  make ipaddrs_ip field UNIQUE in ipaddrs?
  $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddrs_ip = ?';

  ( $error_msg, $sth) = db_handle_query($dbh, $query, $Table{'ipaddrs'}, READ_LOCK, $ipaddrs_ip);
  if ($error_msg) { return( 0 ); }

    # error on no matchs
  if ($sth->rows == 0 ) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'ipaddrs'});
      return( 0 );
  }

  # flatten it out
  while (my @data = $sth->fetchrow_array()) {
          $interface_idx = $data[0];
  }   

  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'ipaddrs'});
      # return the answer
  return ($interface_idx);
}


#######################################################################
### Get the primary key, given a host ip address
# IN:  hostaddrs_ip
# OUT: hostaddrs_id
#######################################################################
sub hostaddr_to_idx
{
  my ($hostaddrs_ip) = @_;
  my ($query, $error_msg, $sth, $dbh);
  my ($hostaddrs_id);

  ( $error_msg, $dbh ) = database_connect($Dbname);
  if ( $error_msg ) { return(0); }


     # TODO:  make hostaddrs_ip field UNIQUE in hostaddrs?
  $query = 'SELECT hostaddrs_id FROM hostaddrs WHERE hostaddrs_ip = ?';

  ( $error_msg, $sth) = db_handle_query($dbh, $query, $Table{'hostaddrs'}, READ_LOCK, $hostaddrs_ip);
  if ($error_msg) { return( 0 ); }

    # error on no matches
  if ($sth->rows == 0 ) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'hostaddrs'});
      return( 0 );
  }

  # flatten it out
  while (my @data = $sth->fetchrow_array()) {
          $hostaddrs_id = $data[0];
  }   

  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'hostaddrs'});
      # return the answer
  return ($hostaddrs_id);
}


#######################################################################
## Check the db ifaces for a router iface ip.  Called from the scheduler
## to see if a router is an edge router.
# IN: ip
# OUT: router idx
#######################################################################
sub check_db_rtr {

    my ($rtr) = @_;
    my ($dbh, $query, $sth, $id, $error_msg);

    ( $error_msg, $dbh ) = database_connect($Dbname);
    if ( $error_msg ) { return(0); }

    $query = 'SELECT * FROM ipaddrs WHERE ipaddrs_ip = ?';
    ( $error_msg, $sth) = db_handle_query($dbh, $query, $Table{'ipaddrs'}, READ_LOCK, $rtr);
    if ( $error_msg ) { return(0); }

    # no match
    if ($sth->rows == 0 ) {
        #print "nothing matched ($rtr)\n";
        db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'ipaddrs'});
        return 0;
    }

    # flatten it out
    while (my @data = $sth->fetchrow_array()) {
            $id = $data[2];
    }   

    # close it up
    db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'ipaddrs'});

    # return the answer
    return $id;
}


## last line of a module
1;
# vim: et ts=4 sw=4
