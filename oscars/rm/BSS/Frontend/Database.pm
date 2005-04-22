# Database.pm:  BSS specific database settings and routines
# Last modified: April 21, 2005
# Jason Lee (jrlee@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Database;

use strict; 

use lib '../..';

use OSCARS_db;

our @ISA = qw(OSCARS_db);


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

##### Settings Begin (Global variables) #####

our( %table, @resv_field_order );

# reservations field names
%table = (
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

@resv_field_order = ('id', 'start_time', 'end_time', 'created_time',
    'bandwidth', 'class', 'burst_limit', 'status', 'ingress_id', 'egress_id',
    'src_id', 'dst_id', 'dn', 'ingress_port', 'egress_port', 'dscp',
    'description');



######################################################################
sub get_BSS_table  {
  my( $self, $table_name ) = @_;
  return ( %table )
}


######################################################################
sub get_resv_field_order {
  my ($self) = @_;
  return (@resv_field_order)
}

    
######################################################################
sub find_pending_reservations  {

    my ($self, $stime, $status) = @_;
    my ($sth);

    #print "pending: Looking at time == $stime \n";

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM reservations WHERE reservation_status = ? and reservation_start_time < ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $status, $stime );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # return the answer
    return $data
}

######################################################################
sub find_expired_reservations  {

    my ($self, $stime, $status) = @_;
    my ($sth);

    #print "expired: Looking at time == " . $stime . "\n";

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM reservations WHERE reservation_status = ? and reservation_end_time < ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $status, $stime );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # return the answer
    return $data
}

######################################################################
sub ipidx2ip {

    my ($self, $idx) = @_;
    my ($sth);

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM ipaddrs WHERE ipaddrs_id = ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $idx );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # XXX: how do we raise an error here? die?
    if ( $#{$data}  == -1 ) {
        return -1;
    }
    #print "ip: " . $data->[0]{'ipaddrs_ip'} . "\n";
    # return the answer
    return $data->[0]{'ipaddrs_ip'}
}

######################################################################
sub hostidx2ip {

    my ($self, $idx) = @_;
    my ($sth);

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM hostaddrs WHERE hostaddrs_id = ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $idx );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # XXX: how do we raise an error here? die?
    if ( $#{$data}  == -1 ) {
        return -1;
    }
    #print "hostip: " . $data->[0]{'hostaddrs_ip'} . "\n";
    # return the answer
    return $data->[0]{'hostaddrs_ip'}
}


######################################################################
sub db_update_reservation {

    my ($self, $res_id, $status) = @_;

    my $sth = $self->{'dbh'}->prepare( qq{ UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;
    $sth->execute( $status, $res_id->{reservation_id});

    # close it up
    $sth->finish;

    return 1;
}


#######################################################################
### Get the interface idx, given a router ip address
# IN:  ipaddrs_ip
# OUT: interface_idx
#######################################################################
sub ipaddr_to_iface_idx  {

  my ($self, $ipaddrs_ip) = @_;
  my ($query, $error_msg, $sth, $dbh);
  my ($interface_idx);

     # TODO:  make ipaddrs_ip field UNIQUE in ipaddrs?
  $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddrs_ip = ?';

  ( $error_msg, $sth) = $self->handle_query($query, 'ipaddrs', $ipaddrs_ip);
  if ($error_msg) { return( 0 ); }

    # error on no matchs
  if ($sth->rows == 0 ) {
      $self->handle_finish( 'ipaddrs' );
      return( 0 );
  }

  # flatten it out
  while (my @data = $sth->fetchrow_array()) {
          $interface_idx = $data[0];
  }   

  $self->handle_finish( 'ipaddrs' );
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
  my ($self, $hostaddrs_ip) = @_;
  my ($query, $error_msg, $sth);
  my ($hostaddrs_id);

     # TODO:  make hostaddrs_ip field UNIQUE in hostaddrs?
  $query = 'SELECT hostaddrs_id FROM hostaddrs WHERE hostaddrs_ip = ?';

  ( $error_msg, $sth) = $self->handle_query($query, 'hostaddrs', $hostaddrs_ip);
  if ($error_msg) { return( 0 ); }

    # if no matches, insert a row in hostaddrs
  if ($sth->rows == 0 ) {
      $query = "INSERT INTO hostaddrs VALUES ( '', '$hostaddrs_ip'  )";
      print STDERR $query, "\n";

      ( $error_msg, $sth) = $self->handle_query($query, 'reservations');
      if ( $error_msg ) { return( 0 ); }
		
      $hostaddrs_id = $self->{'dbh'}->{'mysql_insertid'};
  }
  else {
      # flatten it out
      while (my @data = $sth->fetchrow_array()) {
          $hostaddrs_id = $data[0];
      }   
  }

  $self->handle_finish( 'hostaddrs' );
      # return the answer
  return ($hostaddrs_id);
}


#######################################################################
## Check the db ifaces for a router iface ip.  Called from the scheduler
## to see if a router is an edge router.
# IN: ip
# OUT: interface idx
#######################################################################
sub check_db_rtr {

  my ($self, $rtr) = @_;
  my ($query, $sth, $interface_idx, $error_msg);

  $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddrs_ip = ?';
  ( $error_msg, $sth) = $self->handle_query($query, 'ipaddrs', $rtr);
  if ( $error_msg ) { return(0); }

    # no match
  if ($sth->rows == 0 ) {
      $self->handle_finish( 'ipaddrs' );
      return 0;
  }

    # flatten it out
  while (my @data = $sth->fetchrow_array()) {
      $interface_idx = $data[0];
  }   
  $self->handle_finish( 'ipaddrs' );

    # return the answer
  return $interface_idx;
}


## last line of a module
1;
# vim: et ts=4 sw=4
