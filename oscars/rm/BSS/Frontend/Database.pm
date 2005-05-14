# Database.pm:  BSS specific database settings and routines
# Last modified: May 9, 2005
# Jason Lee (jrlee@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Database;

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
    my %attr = (
        RaiseError => 0,
        PrintError => 0
    );
    $_self->{'dbh'} = DBI->connect($_self->{'configs'}->{'use_BSS_database'}, 
             $_self->{'configs'}->{'BSS_login_name'}, $_self->{'configs'}->{'BSS_login_passwd'})
            or die "Couldn't connect to database: " . DBI->errstr;
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

    
#######################################################################
## Get the db iface id from an ip address.  Called from the scheduler
## to see if a router is an edge router (will not be in ipaddrs table
## if it is not).
# IN:  interface ip address
# OUT: interface id
#######################################################################
sub ip_to_xface_id {
    my ($self, $ipaddr) = @_;
    my ($query, $sth, $interface_id, $error_msg);

    $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddrs_ip = ?';
    $sth = $self->{'dbh'}->prepare( $query );
    if (!$sth) {
        $error_msg = "Can't prepare statement\n" . $self->{'dbh'}->errstr;
        return ( 1, $error_msg );
    }
    $sth->execute( $ipaddr );
    if ( $sth->errstr ) {
        $sth->finish();
        $error_msg = "[ERROR] While converting from ip to interface id:  $sth->errstr";
        return( 1, $error_msg );
    }
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $ipaddr");
    }
    my @data = $sth->fetchrow_array();
    $interface_id = $data[0];
    $sth->finish();
    return ($interface_id, "");
}


#######################################################################
## Get the rooter loopback ip from the interface database id.
# IN:  interface primary key id
# OUT: loopback ip address
#######################################################################
sub xface_id_to_loopback {
    my ($self, $interface_id) = @_;
    my ($query, $sth, $loopback_addr, $error_msg);

    $query = 'SELECT router_name FROM routers WHERE router_id = (SELECT router_id from interfaces WHERE interface_id = ?)';
    $sth = $self->{'dbh'}->prepare( $query );
    if (!$sth) {
        $error_msg = "Can't prepare statement\n" . $self->{'dbh'}->errstr;
        return ( 1, $error_msg );
    }
    $sth->execute( $interface_id );
    if ( $sth->errstr ) {
        $sth->finish();
        $error_msg = "[ERROR] While converting from idx to ip:  $sth->errstr";
        return( 1, $error_msg );
    }

    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $interface_id");
    }
    my @data = $sth->fetchrow_array();
    $loopback_addr = $data[0];
    $sth->finish();
    return ($loopback_addr, "");
}


#######################################################################
### Get the primary key in the hostaddrs table, given a host ip address
### A row is created if that ip address is not present.
# IN:  hostaddrs_ip
# OUT: hostaddrs_id
#######################################################################
sub hostaddrs_ip_to_id
{
    my ($self, $ipaddr) = @_;
    my ($query, $error_msg, $sth);
    my ($id);

    # TODO:  make hostaddrs_ip field UNIQUE in hostaddrs?
    $query = 'SELECT hostaddrs_id FROM hostaddrs WHERE hostaddrs_ip = ?';

    $sth = $self->{'dbh'}->prepare( $query );
    if (!$sth) {
        $error_msg = "Can't prepare statement\n" . $self->{'dbh'}->errstr;
        return ( 1, $error_msg );
    }
    $sth->execute( $ipaddr );
    if ( $sth->errstr ) {
        $sth->finish();
        $error_msg = "[ERROR] While fetching host ip:  $sth->errstr";
        return( 1, $error_msg );
    }

    # if no matches, insert a row in hostaddrs
    if ($sth->rows == 0 ) {
        $query = "INSERT INTO hostaddrs VALUES ( '', '$ipaddr'  )";
        print STDERR '-- ', $query, "\n";

        $sth = $self->{'dbh'}->prepare( $query );
        if (!$sth) {
            $error_msg = "Can't prepare statement\n" . $self->{'dbh'}->errstr;
            return ( 1, $error_msg );
        }
        $sth->execute();
        if ( $sth->errstr ) {
            $sth->finish();
            $error_msg = "[ERROR] While fetching host ip:  $sth->errstr";
            return( 1, $error_msg );
        }
        $id = $self->{'dbh'}->{'mysql_insertid'};
    }
    else {
        my @data = $sth->fetchrow_array();
        $id = $data[0];
    }

    $sth->finish();
    return ($id);
}


#######################################################################
### Get the ip address from the row in the hostaddrs table identified
### by the id.
# IN:  hostaddrs_id
# OUT: hostaddrs_ip
#######################################################################
sub hostaddrs_id_to_ip {
    my ($self, $id) = @_;
    my ($query, $sth, $ipaddr, $error_msg);

    $query = 'SELECT hostaddrs_ip FROM hostaddrs WHERE hostaddrs_id = ?';
    $sth = $self->{'dbh'}->prepare( $query );
    if (!$sth) {
        $error_msg = "Can't prepare statement\n" . $self->{'dbh'}->errstr;
        return ( 1, $error_msg );
    }
    $sth->execute( $id );
    if ( $sth->errstr ) {
        $sth->finish();
        $error_msg = "[ERROR] While converting from host id to ip:  $sth->errstr";
        return( 1, $error_msg );
    }

    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $id");
    }
    my @data = $sth->fetchrow_array();
    $ipaddr = $data[0];
    $sth->finish();
    return ($ipaddr, "");
}


## last line of a module
1;
# vim: et ts=4 sw=4
